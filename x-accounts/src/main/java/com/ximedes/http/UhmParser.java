package com.ximedes.http;

import static com.ximedes.Status.ACCOUNT_NOT_FOUND;
import static com.ximedes.Status.CONFIRMED;
import static com.ximedes.Status.INSUFFICIENT_FUNDS;
import static com.ximedes.Status.PENDING;
import static java.lang.Integer.parseInt;

import com.ximedes.Account;
import com.ximedes.Status;
import com.ximedes.Transaction;

/**
 * A ... uhm ... string and JSON parser, using a hacksaw.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class UhmParser {
    /**
     * Chop the last bit off of an URI and use that as an integer value.
     * 
     * @param uri
     *            The URI to ... uhm ... parse.
     * @return The integer value of the last bit of the URI.
     */
    public static int uhmParseUriLastInteger(final String uri) {
        final int lastSlash = uri.lastIndexOf('/');

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
     *            The JSON to ... uhm ... parse.
     * @return The transfer information.
     */
    public static Transaction uhmParseJsonTransfer(final StringBuilder json) {
        int transactionId = -1;
        int from = -1;
        int to = -1;
        int amount = -1;
        Status status = null;

        int currentChar = 0;
        do {
            char token = '*';
            int value = -1;

            char c;
            do {
                c = json.charAt(currentChar++);
                if (c == 'f' || c == 't' || c == 'a' || c == 's') {
                    // look ahead to see if this is "to" or "transaction"
                    if (c == 't' && json.charAt(currentChar) == 'r') {
                        token = 'x';
                    } else {
                        token = c;
                    }
                }
            } while (token == '*');

            if (token == 's') {
                do {
                    c = json.charAt(currentChar++);
                    if (c == 'P' || c == 'C' || c == 'I' || c == 'A') {
                        value = c;
                    }
                } while (value == -1);
                do {
                    c = json.charAt(currentChar++);
                } while (c == '_' || (c >= 'A' && c <= 'Z'));
            } else {
                // parse numeric value
                do {
                    c = json.charAt(currentChar++);
                    if (c >= '0' && c <= '9') {
                        value = c - '0';
                    }
                } while (value == -1);
                while (c >= '0' && c <= '9') {
                    c = json.charAt(currentChar++);
                    if (c >= '0' && c <= '9') {
                        value = (value * 10) + (c - '0');
                    }
                }
            }

            switch (token) {
            case 'x':
                transactionId = value;
                break;
            case 'f':
                from = value;
                break;
            case 't':
                to = value;
                break;
            case 'a':
                amount = value;
                break;
            case 's':
                switch (value) {
                case 'P':
                    status = PENDING;
                    break;
                case 'C':
                    status = CONFIRMED;
                    break;
                case 'I':
                    status = INSUFFICIENT_FUNDS;
                    break;
                case 'A':
                    status = ACCOUNT_NOT_FOUND;
                    break;
                default:
                    throw new IllegalStateException("status is " + value);
                }
                break;
            default:
                throw new IllegalStateException("token is " + token);
            }
        } while (currentChar < (json.length() - 8));

        return new Transaction(transactionId, from, to, amount, status);
    }
}
