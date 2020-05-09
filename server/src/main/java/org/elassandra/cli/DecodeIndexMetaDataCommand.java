package org.elassandra.cli;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.utils.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.cli.LoggingAwareCommand;
import org.elasticsearch.cli.Terminal;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.xcontent.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.cluster.metadata.MetaData.CONTEXT_CASSANDRA_PARAM;

public class DecodeIndexMetaDataCommand extends LoggingAwareCommand {

    private static final Logger logger = LogManager.getLogger(DecodeIndexMetaDataCommand.class);

    private final OptionSpec<String> smileOption = parser.acceptsAll(Arrays.asList("s", "smile"), "smile to decode (in hex format)").withRequiredArg().required().ofType(String.class);

    public DecodeIndexMetaDataCommand() {
        super("Command to decode the index metadata form a table extension");
    }

    @Override
    protected void execute(Terminal terminal, OptionSet options) throws Exception {
        final String smile = options.valueOf(smileOption);
        terminal.println(String.format("Decoding : [%s]", smile));
        terminal.println(convertToIndexMetaData(smile));
    }

    private String convertToIndexMetaData(String smile) {
        final byte[] bytes = Hex.hexToBytes(smile.startsWith("0x") ? smile.substring(2) : smile);
        return convertToIndexMetaData(bytes);
    }

    public final String convertToIndexMetaData(byte[] bytes) {
        try{
            IndexMetaData indexMetaData = IndexMetaData.FORMAT.loadLatestState(logger, new NamedXContentRegistry(Collections.emptyList()), bytes);

            XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
            builder.prettyPrint();
            builder.startObject();

            Map<String, String> params = new HashMap<>(1);
            params.put(CONTEXT_CASSANDRA_PARAM, "true");
            IndexMetaData.Builder.toXContent(indexMetaData, builder, new ToXContent.MapParams(params));
            builder.endObject();

            builder.flush();
            return ((ByteArrayOutputStream)builder.getOutputStream()).toString("UTF-8");
        } catch (IOException e) {
            throw new InvalidRequestException(String.format("Error while converting smile to json : %s", e.getMessage()));
        }
    }
}
