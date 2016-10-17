package org.json.simple;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.json.simple.parser.JSONParser;

/**
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public final class JSONValue {
    /**
     * parse into java object from input source.
     * 
     * @param in
     *            The stream to read from.
     * @return instance of : JSONObject,JSONArray,String,Boolean,Long,Double or
     *         null
     * @throws IOException
     *             When the input stream could not be read by the parser.
     */
    public static Object parse(final Reader in) throws IOException {
        final JSONParser parser = new JSONParser();
        return parser.parse(in);
    }

    /**
     * Parse a JSON value.
     * 
     * @param s
     *            the string to parse.
     * @return The parsed object.
     * @throws IOException
     *             When the input stream could not be read by the parser.
     */
    public static Object parse(final String s) throws IOException {
        StringReader in = new StringReader(s);
        return parse(in);
    }
}
