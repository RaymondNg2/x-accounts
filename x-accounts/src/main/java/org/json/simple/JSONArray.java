package org.json.simple;

import static org.json.simple.JSONObject.printValue;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * A specialised array list that will behave as a JSON array.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONArray extends ArrayList<Object> {
    private static final long serialVersionUID = -3087711412615230311L;

    /**
     * Write the array into a stream.
     * 
     * @param writer
     *            The stream to write into.
     */
    public void write(final PrintWriter writer) {
        final StringBuilder out = new StringBuilder();
        print(out);
        writer.write(out.toString());
    }

    void print(final StringBuilder out) {
        out.append('[');

        boolean first = true;
        for (final Object value : this) {
            if (first) {
                first = false;
            } else {
                out.append(',');
            }

            printValue(out, value);
        }

        out.append(']');
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        print(out);
        return out.toString();
    }
}
