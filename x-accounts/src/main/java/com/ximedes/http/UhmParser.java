package com.ximedes.http;

import static java.lang.Integer.parseInt;

import java.util.Map;

import org.eclipse.jetty.util.ajax.JSON;

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

    public static Transaction uhmParseJsonTransfer(final StringBuilder json) {
        @SuppressWarnings("unchecked")
        final Map<String, ?> fields = (Map<String, ?>) JSON
                .parse(json.toString());

        final int from = parseInt(((String) fields.get("from")).trim());
        final int to = parseInt(((String) fields.get("to")).trim());
        final int amount = ((Long) fields.get("amount")).intValue();

        return new Transaction(-1, from, to, amount, null);
    }
}
