package org.elassandra.cli;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.utils.Hex;
import org.elasticsearch.cli.LoggingAwareCommand;
import org.elasticsearch.cli.Terminal;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.xcontent.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.cluster.metadata.MetaData.CONTEXT_CASSANDRA_PARAM;

public class DecodeSmileCommand extends LoggingAwareCommand {

    private final JsonFactory jsonFactory = new JsonFactory();
    private final SmileFactory smileFactory = new SmileFactory();

    private final OptionSpec<String> smileOption = parser.acceptsAll(Arrays.asList("s", "smile"), "smile to decode (in hex format)").withRequiredArg().required().ofType(String.class);

    public DecodeSmileCommand() {
        super("Command to decode the smile form of en ElasticSearch index description");
    }

    @Override
    protected void execute(Terminal terminal, OptionSet options) throws Exception {
        final String smile = options.valueOf(smileOption);
        terminal.println(String.format("Decoding : [%s]", smile));
        terminal.println(convertToJson(smile));
    }

    private String convertToJson(String smile) {
        final byte[] bytes = Hex.hexToBytes(smile.startsWith("0x") ? smile.substring(2) : smile);
        return convertToJson(bytes);
    }

    public final String convertToJson(byte[] bytes) {
        try{
            IndexMetaData indexMetaData = IndexMetaData.FORMAT.loadLatestState(null, new NamedXContentRegistry(Collections.emptyList()), bytes);

            XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
            builder.prettyPrint();
            builder.startObject();

            Map<String, String> params = new HashMap<>(1);
            params.put(CONTEXT_CASSANDRA_PARAM, "true");
            IndexMetaData.Builder.toXContent(indexMetaData, builder, new ToXContent.MapParams(params));

            builder.endObject();

            return builder.bytes().utf8ToString();
        } catch (IOException e) {
            throw new InvalidRequestException(String.format("Error while converting smile to json : %s", e.getMessage()));
        }
    }
}
