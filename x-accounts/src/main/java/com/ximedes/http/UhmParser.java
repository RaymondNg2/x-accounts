package com.ximedes.http;

import static java.lang.Integer.parseInt;

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

    /**
     * A hacksaw parser for JSON. We build on the fact that we know that we are
     * looking for 6 tokens: "from", "to" and "amount" and three numeric values.
     * So what we do is quickly scan the JSON for those. In fact, for the field
     * names we only scan for the first letter (f, t, a), so we make a *lot* of
     * assumptions about the input.
     * 
     * @param json
     *            The JSON to parse.
     * @return The transfer information.
     */
    public static Transaction uhmParseJsonTransfer(final StringBuilder json) {
        int from = -1;
        int to = -1;
        int amount = -1;
        int currentChar = 0;
        for (int i = 0; i < 3; i++) {
            char token = '*';
            int value = -1;

            char c;
            do {
                c = json.charAt(currentChar++);
                // System.err.println(
                //          "a." + i + ": @" + currentChar + " [" + c + "]"); // XXX
                if (c == 'f' || c == 't' || c == 'a') {
                    token = c;
                }
            } while (token == '*');

            do {
                c = json.charAt(currentChar++);
//                System.err.println(
//                        "b." + i + ": @" + currentChar + " [" + c + "]"); // XXX
                if (c >= '0' && c <= '9') {
                    value = c - '0';
                }
            } while (value == -1);
            while (c >= '0' && c <= '9') {
                c = json.charAt(currentChar++);
//                System.err.println(
//                        "c." + i + ": @" + currentChar + " [" + c + "]"); // XXX
                if (c >= '0' && c <= '9') {
                    value = (value * 10) + (c - '0');
                }
            }

            switch (token) {
            case 'f':
                from = value;
                break;
            case 't':
                to = value;
                break;
            case 'a':
                amount = value;
                break;
            default:
                throw new IllegalStateException("token is " + token);
            }
        }

        return new Transaction(-1, from, to, amount, null);
    }
}
