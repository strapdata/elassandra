/*
 * Copyright (c) 2017 Strapdata (http://www.strapdata.com)
 * Contains some code from Elasticsearch (http://www.elastic.co)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cassandra.service;

import static com.google.common.collect.Sets.newHashSet;

import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.utils.FBUtilities;
import org.elassandra.NoPersistedMetaDataException;
import org.elassandra.cluster.InternalCassandraClusterService;
import org.elassandra.discovery.CassandraDiscovery;
import org.elassandra.index.ElasticSecondaryIndex;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.Version;
import org.elasticsearch.bootstrap.Bootstrap;
import org.elasticsearch.bootstrap.JVMCheck;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.cli.Terminal;
import org.elasticsearch.common.inject.CreationException;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.spi.Message;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.logging.logback.LogbackESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.monitor.jvm.JvmInfo;
import org.elasticsearch.monitor.process.ProcessProbe;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.node.internal.InternalSettingsPreparer;
import org.elasticsearch.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstrap sequence Cassandra setup() joinRing() beforBoostrap() -> wait for
 * local shard = STARTED ElasticSearch activate() GatewayService recover
 * metadata from cassandra schema DiscoveryService discover ring topology and
 * build routing table await that Cassandra start() (Complet cassandra
 * bootstrap) ElasticSearch start() (Open Elastic http service)
 * 
 * 
 * @author vroyer
 *
 */
public class ElassandraDaemon extends CassandraDaemon {
    private static final Logger logger = LoggerFactory.getLogger(ElassandraDaemon.class);

    private static volatile Thread keepAliveThread;
    private static volatile CountDownLatch keepAliveLatch;

    public static ElassandraDaemon instance = new ElassandraDaemon();
    public static boolean hasWorkloadColumn = false;
    
    private Node node = null;
    private Settings settings;
    private Environment env;
    private boolean boostraped = false;
    private MetaData systemMetadata = null;
    
    static {
        try {
            ESLoggerFactory.setDefaultFactory(new LogbackESLoggerFactory());
        } catch (Exception e) {
            System.err.println("Failed to configure logging "+e.toString());
        }
    }

    public ElassandraDaemon() {
        super(true);
    }
    
    public Node node() {
        return node;
    }
    
    public void activate(boolean addShutdownHook, Settings settings, Environment env, Collection<Class<? extends Plugin>> pluginList) {
        instance.setup(addShutdownHook, settings, env, pluginList); 
        
        //enable indexing in cassandra.
        ElasticSecondaryIndex.runsElassandra = true;
        
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(new StandardMBean(new NativeAccess(), NativeAccessMBean.class), new ObjectName(MBEAN_NAME));
        } catch (Exception e) {
            logger.error("error registering MBean {}", MBEAN_NAME, e);
            // Allow the server to start even if the bean can't be registered
        }
        
        // Set workload it to "elasticsearch"
        try {
            ColumnIdentifier workload = new ColumnIdentifier("workload",false);
            CFMetaData local = SystemKeyspace.metadata().getTableOrViewNullable(SystemKeyspace.LOCAL);
            if (local.getColumnDefinition(workload) != null) {
                QueryProcessor.executeOnceInternal("INSERT INTO system.local (key, workload) VALUES (?,?)" , new Object[] { "local","elasticsearch" });
                logger.info("Internal workload set to elasticsearch");
                
                CFMetaData system = SystemKeyspace.metadata().getTableOrViewNullable(SystemKeyspace.LOCAL);
                hasWorkloadColumn = system.getColumnDefinition(workload) != null;
            }
        } catch (Exception e1) {
            logger.error("Failed to set the workload to elasticsearch.",e1);
        }
        
        super.setup(); // start bootstrap CassandraDaemon 
        super.start(); // start Thrift+RPC service

        if (instance.node != null) {
            instance.node.activate();
            instance.node.clusterService().submitNumberOfShardsUpdate();
            instance.node.start();
        } else {
            logger.error("Cannot start elasticsearch, initialization failed. You are probably using the CassandraDeamon.class form Apache Cassandra rather than the one provided with Elassandra. Please check you classpth.");
        }
    }

    /**
     * Start Node if metadata available.
     */
    @Override
    public void systemKeyspaceInitialized() {
        try {
            systemMetadata = node.clusterService().readMetaDataAsComment();
            if (node != null && systemMetadata != null) {
                logger.debug("Starting Elasticsearch shards before open user keyspaces...");
                node.clusterService().addShardStartedBarrier();
                node.activate();
                node.clusterService().blockUntilShardsStarted();
            }
        } catch(NoPersistedMetaDataException e) {
            logger.debug("Start Elasticsearch later, no mapping available");
        } catch(Throwable e) {
            logger.warn("Unexpected error",e);
        }
        
    }
    
    @Override
    public void userKeyspaceInitialized() {
        ElasticSecondaryIndex.userKeyspaceInitialized = true;
        
        final ClusterService clusterService = node.clusterService();
        clusterService.submitStateUpdateTask("user-keyspaces-initialized",new ClusterStateUpdateTask() {
            @Override
            public ClusterState execute(ClusterState currentState) {
                ClusterState newClusterState = clusterService.updateNumberOfShards( currentState );
                return ClusterState.builder(newClusterState).incrementVersion().build();
            }

            @Override
            public void onFailure(String source, Throwable t) {
                logger.error("unexpected failure during [{}]", t, source);
            }
        });
    }
    
    @Override
    public void beforeBootstrap() {
        boostraped = true;
        node.activate();
    }
    
    @Override
    public void ringReady() {
        node.activate();
    }

    /**
     * hook for JSVC
     */
    public void start() {
        super.start();
    }

    /**
     * hook for JSVC
     */
    public void stop() {
        super.stop();
        if (node != null) 
            node.close();
    }

    /**
     * hook for JSVC
     */
    public void init(String[] args) {

    }

    /**
     * hook for JSVC
     */
    public void activate() {
        node.activate();
    }
    
    /**
     * hook for JSVC
     */
    public void destroy() {
        super.destroy();
        if (node != null)
            node.close();
        if (keepAliveLatch != null)
            keepAliveLatch.countDown();
    }

    public void setup(boolean addShutdownHook, Settings settings, Environment environment, Collection<Class<? extends Plugin>> pluginList) {
        this.settings = settings;
        this.env = environment;
        org.elasticsearch.bootstrap.Bootstrap.initializeNatives(
                          env.tmpFile(),
                          settings.getAsBoolean("bootstrap.mlockall", false),
                          settings.getAsBoolean("bootstrap.seccomp", true),
                          settings.getAsBoolean("bootstrap.ctrlhandler", true));

        // initialize probes before the security manager is installed
        org.elasticsearch.bootstrap.Bootstrap.initializeProbes();

        if (addShutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (node != null) {
                        node.close();
                    }
                }
            });
        }
      
        // look for jar hell
        /*
        try {
            JarHell.checkJarHell();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
         */
        
        // install SM after natives, shutdown hooks, etc.
        //org.elasticsearch.bootstrap.Bootstrap.setupSecurity(settings, environment);

        // We do not need to reload system properties here as we have already applied them in building the settings and
        // reloading could cause multiple prompts to the user for values if a system property was specified with a prompt
        // placeholder
        Settings nodeSettings = Settings.settingsBuilder()
                .put(settings)
                .put(InternalSettingsPreparer.IGNORE_SYSTEM_PROPERTIES_SETTING, true)
                .build();
        
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().settings(nodeSettings);
        
        String clusterName = DatabaseDescriptor.getClusterName();
        String datacenterGroup = settings.get(InternalCassandraClusterService.SETTING_CLUSTER_DATACENTER_GROUP);
        if (datacenterGroup != null) {
            clusterName = DatabaseDescriptor.getClusterName() + "@" + datacenterGroup.trim();
        }
        nodeBuilder.clusterName(clusterName).data(true).settings()
                .put("name", CassandraDiscovery.buildNodeName())
                .put("network.bind_host", DatabaseDescriptor.getRpcAddress().getHostAddress())
                .put("network.publish_host", FBUtilities.getBroadcastRpcAddress().getHostAddress())
                .put("transport.bind_host", DatabaseDescriptor.getRpcAddress().getHostAddress())
                .put("transport.publish_host", FBUtilities.getBroadcastRpcAddress().getHostAddress())
                //.put("http.netty.bind_host", DatabaseDescriptor.getRpcAddress().getHostAddress())
                //.put("http.bind_host", DatabaseDescriptor.getRpcAddress().getHostAddress())
                //.put("http.host", DatabaseDescriptor.getRpcAddress().getHostAddress())
                ;

        this.node = nodeBuilder.build(pluginList);
    }
  
    public static Client client() {
        if ((instance.node != null) && (!instance.node.isClosed()))
            return instance.node.client();
        return null;
    }

    public static Injector injector() {
        if ((instance.node != null) && (!instance.node.isClosed()))
            return instance.node.injector();
        return null;
    }

    public static String getHomeDir() {
        String cassandra_home = System.getenv("CASSANDRA_HOME");
        if (cassandra_home == null) {
            cassandra_home = System.getProperty("cassandra.home", System.getProperty("path.home",Paths.get("").toAbsolutePath().toString()));
        }
        return cassandra_home;
    }
    
    public static String getConfigDir() {
        String cassandra_conf = System.getenv("CASSANDRA_CONF");
        if (cassandra_conf == null) {
            cassandra_conf = System.getProperty("cassandra.conf", System.getProperty("path.conf",getHomeDir()+"/conf"));
        }
        return cassandra_conf;
    }
    
    public static String getElastisearchDataDir() {
        String cassandra_storagedir = System.getProperty("cassandra_storagedir");
        if (cassandra_storagedir == null) {
            cassandra_storagedir = System.getProperty("path.data",getHomeDir()+"/data/elasticsearch.data");
        } else {
            cassandra_storagedir = cassandra_storagedir + "/elasticsearch.data";
        }
        return cassandra_storagedir;
    }
    
    public static void main(String[] args) {
        
        try
        {
            DatabaseDescriptor.forceStaticInitialization();
        }
        catch (ExceptionInInitializerError e)
        {
            System.out.println("Exception (" + e.getClass().getName() + ") encountered during startup: " + e.getMessage());
            String errorMessage = buildErrorMessage("Initialization", e);
            System.err.println(errorMessage);
            System.err.flush();
            System.exit(3);
        }
        
        boolean foreground = System.getProperty("cassandra-foreground") != null;
        // handle the wrapper system property, if its a service, don't run as a
        // service
        if (System.getProperty("wrapper.service", "XXX").equalsIgnoreCase("true")) {
            foreground = false;
        }

        
        
        if (System.getProperty("es.max-open-files", "false").equals("true")) {
            ESLogger logger = Loggers.getLogger(Bootstrap.class);
            logger.info("max_open_files [{}]", ProcessProbe.getInstance().getMaxFileDescriptorCount());
        }

        // warn if running using the client VM
        if (JvmInfo.jvmInfo().getVmName().toLowerCase(Locale.ROOT).contains("client")) {
            ESLogger logger = Loggers.getLogger(Bootstrap.class);
            logger.warn("jvm uses the client vm, make sure to run `java` with the server vm for best performance by adding `-server` to the command line");
        }

        String stage = "Initialization";

        try {
            if (!foreground) {
                Loggers.disableConsoleLogging();
                System.out.close();
            }

            // fail if using broken version
            JVMCheck.check();

            Environment env = InternalSettingsPreparer.prepareEnvironment(
                    Settings.settingsBuilder()
                        .put("node.name","node0")
                        .put("path.home",getHomeDir())
                        .put("path.conf",getConfigDir())
                        .put("path.data",getElastisearchDataDir())
                        .build(), 
                    foreground ? Terminal.DEFAULT : null);
            
            instance.activate(true, env.settings(), env,  Collections.<Class<? extends Plugin>>emptyList());
            if (!foreground) {
                System.err.close();
            }

            keepAliveLatch = new CountDownLatch(1);
            // keep this thread alive (non daemon thread) until we shutdown
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    keepAliveLatch.countDown();
                }
            });

            keepAliveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        keepAliveLatch.await();
                    } catch (InterruptedException e) {
                        // bail out
                    }
                }
            }, "elasticsearch[keepAlive/" + Version.CURRENT + "]");
            keepAliveThread.setDaemon(false);
            keepAliveThread.start();
        } catch (Throwable e) {
            ESLogger logger = Loggers.getLogger(ElassandraDaemon.class);
            if (instance.node != null) {
                logger = Loggers.getLogger(ElassandraDaemon.class, instance.node.settings().get("name"));
            }
            String errorMessage = buildErrorMessage(stage, e);
            if (foreground) {
                System.err.println(errorMessage);
                System.err.flush();
                Loggers.disableConsoleLogging();
            }
            logger.error("Exception", e);

            System.exit(3);
        }
    }

    private static String buildErrorMessage(String stage, Throwable e) {
        StringBuilder errorMessage = new StringBuilder("{").append(Version.CURRENT).append("}: ");
        errorMessage.append(stage).append(" Failed ...\n");
        if (e instanceof CreationException) {
            CreationException createException = (CreationException) e;
            Set<String> seenMessages = newHashSet();
            int counter = 1;
            for (Message message : createException.getErrorMessages()) {
                String detailedMessage;
                if (message.getCause() == null) {
                    detailedMessage = message.getMessage();
                } else {
                    detailedMessage = ExceptionsHelper.detailedMessage(message.getCause(), true, 0);
                }
                if (detailedMessage == null) {
                    detailedMessage = message.getMessage();
                }
                if (seenMessages.contains(detailedMessage)) {
                    continue;
                }
                seenMessages.add(detailedMessage);
                errorMessage.append("").append(counter++).append(") ").append(detailedMessage);
            }
        } else {
            errorMessage.append("- ").append(ExceptionsHelper.detailedMessage(e, true, 0));
        }
        if (Loggers.getLogger(ElassandraDaemon.class).isDebugEnabled()) {
            errorMessage.append("\n").append(ExceptionsHelper.stackTrace(e));
        }
        return errorMessage.toString();
    }
}
