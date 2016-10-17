package com.ximedes.http;

import static com.ximedes.utils.SneakyThrows.sneakyThrow;
import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.io.StringReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.ximedes.Transaction;

/**
 * A ... uhm ... string and JSON parser, using a hacksaw.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class UhmParser {
    public static int uhmParseJsonLastInteger(final StringBuilder json) {
        final int lastColon = json.lastIndexOf(":");
        final int lastCloseBrace = json.lastIndexOf("}");

        return parseInt(json.substring(lastColon + 1, lastCloseBrace).trim());
    }

    public static int uhmParseUriLastInteger(final String uri) {
        final int lastSlash = uri.lastIndexOf("/");

        return parseInt(uri.substring(lastSlash + 1).trim());
    }

    private static final JSONParser jsonParser = new JSONParser();

    public static Transaction uhmParseJsonTransfer(final StringBuilder json) {
        try {
            final JSONObject fields = (JSONObject) jsonParser
                    .parse(new StringReader(json.toString()));

        final int from = parseInt(((String) fields.get("from")).trim());
        final int to = parseInt(((String) fields.get("to")).trim());
        final int amount = ((Long) fields.get("amount")).intValue();

        return new Transaction(-1, from, to, amount, null);
        } catch (IOException e) {
            throw sneakyThrow(e);
        }
    }
}
