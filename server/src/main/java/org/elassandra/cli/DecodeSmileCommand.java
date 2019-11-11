package org.elassandra.cli;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.utils.Hex;
import org.elasticsearch.cli.LoggingAwareCommand;
import org.elasticsearch.cli.Terminal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

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
        return new String(convertToJson(bytes));
    }

    public final byte[] convertToJson(byte[] bytes) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try(JsonParser sp = smileFactory.createParser(bytes);
            JsonGenerator jg = jsonFactory.createGenerator(bos).useDefaultPrettyPrinter()) {

            while (sp.nextToken() != null) {
                jg.copyCurrentEvent(sp);
            }

        } catch (IOException e) {
            throw new InvalidRequestException(String.format("Error while converting smile to json : %s", e.getMessage()));
        }
        return bos.toByteArray();
    }
}
