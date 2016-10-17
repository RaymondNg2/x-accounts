package org.json.simple;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.toHexString;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple name/value pair object for JSON translation.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONObject extends HashMap<String, Object> {
    private static final long serialVersionUID = -207250283065983015L;

    /**
     * Write the object into a stream.
     * 
     * @param writer
     *            The stream to write into.
     */
    public void write(final PrintWriter writer) {
        final StringBuilder out = new StringBuilder();
        print(out);
        writer.write(out.toString());
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        print(out);
        return out.toString();
    }

    /**
     * Print a value to a string builder, formatted as JSON. We pick out strings
     * for escaping and dates for formatting, but the rest we just send across
     * as-is.
     * 
     * @param out
     *            The string builder write to.
     * @param value
     *            The value to print.
     */
    static void printValue(final StringBuilder out, final Object value) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String || value instanceof StringBuilder
                || value instanceof Character) {
            printString(out, value.toString());
        } else if (value instanceof JSONObject) {
            ((JSONObject) value).print(out);
        } else if (value instanceof JSONArray) {
            ((JSONArray) value).print(out);
        } else if (value instanceof Date) {
            out.append("Date(");
            out.append(((Date) value).getTime());
            out.append(')');
        } else {
            out.append(value.toString());
        }
    }

    private static void printString(final StringBuilder out, final String s) {
        if (s == null) {
            out.append("null");
            return;
        }

        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
            case '"':
                out.append("\\\"");
                break;
            case '\\':
                out.append("\\\\");
                break;
            case '\b':
                out.append("\\b");
                break;
            case '\f':
                out.append("\\f");
                break;
            case '\n':
                out.append("\\n");
                break;
            case '\r':
                out.append("\\r");
                break;
            case '\t':
                out.append("\\t");
                break;
            case '/':
                out.append("\\/");
                break;
            default:
                if (ch >= '\u0000' && ch <= '\u001F') {
                    final String ss = toHexString(ch);
                    out.append("\\u");
                    for (int k = 0; k < 4 - ss.length(); k++) {
                        out.append('0');
                    }
                    out.append(ss.toUpperCase());
                } else {
                    out.append(ch);
                }
            }
        }
        out.append('"');
    }

    void print(final StringBuilder out) {
        out.append('{');

        boolean first = true;
        for (final Map.Entry<String, Object> entry : entrySet()) {
            if (first) {
                first = false;
            } else {
                out.append(',');
            }

            printString(out, entry.getKey());
            out.append(':');

            printValue(out, entry.getValue());
        }

        out.append('}');
    }

    /**
     * Returns the Boolean value to which the specified key is mapped, or null
     * if this JSONObject contains no mapping for the key or the value does not
     * contain the parsable Boolean.
     * 
     * @param key
     *            The key whose associated value is to be returned
     * @return the Boolean value to which the specified key is mapped, or
     *         <code>null</code> if this JSONObject contains no mapping for the
     *         key, or false if the value does not contain the parsable Boolean.
     */
    public Boolean getBoolean(final String key) {
        try {
            final Boolean value = parseBoolean(get(key).toString());
            return value;
        } catch (NullPointerException e) {
            throw new NullPointerException("null " + key);
        }
    }

    /**
     * Returns the Integer value to which the specified key is mapped, or null
     * if this JSONObject contains no mapping for the key or the value does not
     * contain the parsable Integer.
     * 
     * @param key
     *            The key whose associated value is to be returned
     * @return the Integer value to which the specified key is mapped, or null
     *         if this JSONObject contains no mapping for the key or the value
     *         does not contain the parsable Integer.
     */
    public Integer getInteger(final String key) {
        try {
            final Integer value = parseInt(get(key).toString());
            return value;
        } catch (NullPointerException e) {
            throw new NullPointerException("null " + key + " in " + this);
        } catch (final Exception e) {
            return null;
        }
    }
}
