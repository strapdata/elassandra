package org.apache.cassandra.service;

import static com.google.common.collect.Sets.newHashSet;
import static org.elasticsearch.common.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.service.CassandraDaemon.NativeAccess;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.Version;
import org.elasticsearch.bootstrap.JVMCheck;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.inject.CreationException;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.spi.Message;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.jna.Kernel32Library.ConsoleCtrlHandler;
import org.elasticsearch.common.jna.Natives;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.logging.logback.LogbackESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.DiscoveryModule;
import org.elasticsearch.discovery.cassandra.CassandraDiscovery;
import org.elasticsearch.discovery.cassandra.CassandraDiscoveryModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.gateway.cassandra.CassandraGatewayModule;
import org.elasticsearch.monitor.jvm.JvmInfo;
import org.elasticsearch.monitor.process.JmxProcessProbe;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.node.internal.InternalNode;
import org.elasticsearch.node.internal.InternalSettingsPreparer;

import com.google.common.base.Charsets;

/**
 * Bootstrap sequence
 * Cassandra setup() 
 * 		joinRing()
 * 			beforBoostrap() -> wait for local shard = STARTED
 * 				 ElasticSearch activate()
 * 					GatewayService recover metadata from cassandra schema
 * 					DiscoveryService discover ring topology and build routing table
 * 				 await that 
 * Cassandra start() 		(Complet cassandra bootstrap)
 * ElasticSearch start() 	(Open Elastic http service)
 * 
 * @author vroyer
 *
 */
public class ElastiCassandraDaemon extends CassandraDaemon {
	private Node node;
	private static volatile Thread keepAliveThread;
    private static volatile CountDownLatch keepAliveLatch;

    private static ElastiCassandraDaemon instance = new ElastiCassandraDaemon();
    private Tuple<Settings, Environment> tuple;
    private boolean addShutdownHook;
    
    static {
    	try {
        	ESLoggerFactory.setDefaultFactory(new LogbackESLoggerFactory());
        } catch (Exception e) {
            System.err.println("Failed to configure logging...");
            e.printStackTrace();
        }
    }
    
	public ElastiCassandraDaemon() {
		super();
	}
	
	public void activate(boolean addShutdownHook, Tuple<Settings, Environment> tuple)  {
		this.addShutdownHook = addShutdownHook;
		this.tuple = tuple;
		try
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(new StandardMBean(new NativeAccess(), NativeAccessMBean.class), new ObjectName(MBEAN_NAME));
        }
        catch (Exception e)
        {
            logger.error("error registering MBean {}", MBEAN_NAME, e);
            //Allow the server to start even if the bean can't be registered
        }
		super.setup();		   // start bootstrap CassandraDaemon and call beforeBootstrap to activate ElasticSearch and wait for local shards
		super.start();		   // complete cassandra start
		instance.node.start(); // start ElasticSerach public services to complete
	}

	
	@Override
	public void beforeBootstrap() {
		logger.debug("Starting ElasticSearch, recover metadata from cassandra schema");
		instance.buildNode(addShutdownHook, tuple);	// Initialize ElasticSearch
	    this.node.activate(); // block until recovery and all primary shard started.
		logger.debug("ElasticSearch ready to index, client={}",this.node.client());
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
    	node.stop();
    	super.stop();
    }
    
    
    /**
     * hook for JSVC
     */
    public void init(String[] args)  {

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
        node.close();
        keepAliveLatch.countDown();
    }

    
	
	
    
    private void buildNode(boolean addShutdownHook, Tuple<Settings, Environment> tuple) {
        if (tuple.v1().getAsBoolean("bootstrap.mlockall", false)) {
            Natives.tryMlockall();
        }

        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().settings(tuple.v1()).loadConfigSettings(false);
        nodeBuilder.clusterName( DatabaseDescriptor.getClusterName() )
        	.data(true)
        	.put("gateway.type",CassandraGatewayModule.class.getName())
        	.put(DiscoveryModule.DISCOVERY_TYPE_KEY,CassandraDiscoveryModule.class.getName())
        	.put("name", CassandraDiscovery.buildNodeName(DatabaseDescriptor.getRpcAddress()))
        	.put("network.host", DatabaseDescriptor.getRpcAddress().getHostAddress() );
        
        
        this.node = nodeBuilder.build();
        if (addShutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    node.close();
                }
            });
        }

        if (tuple.v1().getAsBoolean("bootstrap.ctrlhandler", true)) {
            Natives.addConsoleCtrlHandler(new ConsoleCtrlHandler() {
                @Override
                public boolean handle(int code) {
                    if (CTRL_CLOSE_EVENT == code) {
                        ESLogger logger = Loggers.getLogger(ElastiCassandraDaemon.class);
                        logger.info("running graceful exit on windows");

                        System.exit(0);
                        return true;
                    }
                    return false;
                }
            });
        }
        
    }

   
    
    
    
    public static Client  client() {
    	if ((instance.node != null) && (!instance.node.isClosed()) ) 
    		return instance.node.client();
    	return null;
    }
    
    public static Injector injector() {
    	if ((instance.node != null) && (!instance.node.isClosed()) ) 
    		return ((InternalNode)instance.node).injector();
    	return null;
    }

    public static void main(String[] args) {
    	
        final String pidFile = System.getProperty("es.pidfile", System.getProperty("es-pidfile"));

        if (pidFile != null) {
            try {
                File fPidFile = new File(pidFile);
                if (fPidFile.getParentFile() != null) {
                    FileSystemUtils.mkdirs(fPidFile.getParentFile());
                }
                FileOutputStream outputStream = new FileOutputStream(fPidFile);
                outputStream.write(Long.toString(JvmInfo.jvmInfo().pid()).getBytes(Charsets.UTF_8));
                outputStream.close();

                fPidFile.deleteOnExit();
            } catch (Exception e) {
                String errorMessage = buildErrorMessage("pid", e);
                System.err.println(errorMessage);
                System.err.flush();
                System.exit(3);
            }
        }

        boolean foreground = System.getProperty("es.foreground", System.getProperty("es-foreground")) != null;
        // handle the wrapper system property, if its a service, don't run as a service
        if (System.getProperty("wrapper.service", "XXX").equalsIgnoreCase("true")) {
            foreground = false;
        }

        
        Tuple<Settings, Environment> tuple = null;
        try {
        	tuple = InternalSettingsPreparer.prepareSettings(EMPTY_SETTINGS, true);
            //setupLogging(tuple);
        } catch (Exception e) {
            String errorMessage = buildErrorMessage("Setup", e);
            System.err.println(errorMessage);
            System.err.flush();
            System.exit(3);
        }

        if (System.getProperty("es.max-open-files", "false").equals("true")) {
            ESLogger logger = Loggers.getLogger(ElastiCassandraDaemon.class);
            logger.info("max_open_files [{}]", JmxProcessProbe.getMaxFileDescriptorCount());
        }

        // warn if running using the client VM
        if (JvmInfo.jvmInfo().vmName().toLowerCase(Locale.ROOT).contains("client")) {
            ESLogger logger = Loggers.getLogger(ElastiCassandraDaemon.class);
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

            instance.activate(true, tuple);

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
            ESLogger logger = Loggers.getLogger(ElastiCassandraDaemon.class);
            if (instance.node != null) {
                logger = Loggers.getLogger(ElastiCassandraDaemon.class, instance.node.settings().get("name"));
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
        if (Loggers.getLogger(ElastiCassandraDaemon.class).isDebugEnabled()) {
            errorMessage.append("\n").append(ExceptionsHelper.stackTrace(e));
        }
        return errorMessage.toString();
    }
}
