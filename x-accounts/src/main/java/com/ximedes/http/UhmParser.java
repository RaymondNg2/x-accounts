package com.ximedes.http;

import static java.lang.Integer.parseInt;

/**
 * A ... uhm ... string and JSON parser, using a hacksaw.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class UhmParser {

    public static int uhmParseJsonLastInteger(final StringBuilder body) {
        final int lastColon = body.lastIndexOf(":");
        final int lastCloseBrace = body.lastIndexOf("}");

        return parseInt(body.substring(lastColon + 1, lastCloseBrace).trim());
    }

    public static int uhmParseUriLastInteger(final String uri) {
        final int lastSlash = uri.lastIndexOf("/");

        return parseInt(uri.substring(lastSlash + 1).trim());
    }

}
