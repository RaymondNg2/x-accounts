package com.ximedes.http;

import static com.ximedes.Status.CONFIRMED;
import static com.ximedes.http.UhmParser.uhmParseUriLastInteger;
import static com.ximedes.utils.SneakyThrows.sneakyThrow;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.ximedes.API;
import com.ximedes.Transaction;

/**
 * An HTTP API client based on Java's HTTPUrlConnection.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class HttpUrlConnectionApiClient implements API {
    private static final URL accountUrl = mkUrl("account");
    private static final URL transferUrl = mkUrl("transfer");
    private static final URL countUrl = mkUrl("countCentsInTheSystem");

    private static URL mkUrl(final String path) {
        try {
            return new URL("http://localhost:8080/" + path);
        } catch (MalformedURLException e) {
            throw sneakyThrow(e);
        }
    }

    private static final String LOCATION_HEADER = "Location";

    private static final int CUTOFF = 2500; /* milliseconds */

    private static final boolean trace = false;

    /**
     * @see com.ximedes.API#createAccount(int)
     */
    @Override
    public int createAccount(final int overdraft) {
        final long start = currentTimeMillis();

        final String json = "{\"overdraft\":" + overdraft + "}";
        final String locationHeader = postAndReturnLocationHeader(accountUrl,
                json);

        final long responseTime = currentTimeMillis() - start;
        if (responseTime > CUTOFF) {
            out.println("create account took " + responseTime + " ms.");
        }

        return uhmParseUriLastInteger(locationHeader);
    }

    /**
     * @see com.ximedes.API#transfer(int, int, int)
     */
    @Override
    public Transaction transfer(final int from, final int to,
            final int amount) {
        final long start = currentTimeMillis();

        // note that from and to are sent as strings and not as integer
        final String json = "{\"from\":\"" + from + "\",\"to\":\"" + to
                + "\",\"amount\":" + amount + "}";
        final String locationHeader = postAndReturnLocationHeader(transferUrl,
                json);

        final long responseTime = currentTimeMillis() - start;
        if (responseTime > CUTOFF) {
            out.println("transfer took " + responseTime + " ms.");
        }

        // XXX WRONG The transaction status is not actually known at this
        // point. So we need to change the test to fetch the transaction
        // status after the fact.
        return new Transaction(uhmParseUriLastInteger(locationHeader), from, to,
                amount, CONFIRMED);
    }

    /**
     * @see com.ximedes.API#countCentsInTheSystem()
     */
    @Override
    public int countCentsInTheSystem() {
        final JSONObject response = getAndParseBodyAsJson(countUrl);
        return ((Number) response.get("cents")).intValue();
    }

    // --- the HTTP client implementation is below

    private final JSONParser jsonParser = new JSONParser();

    private String postAndReturnLocationHeader(final URL url,
            final String json) {
        try {
            final HttpURLConnection response = sendRequest("POST", url, json,
                    true);
            final String locationHeader = response
                    .getHeaderField(LOCATION_HEADER);

            // Getting an immediately closing the input stream may seem a bit
            // odd, but it closes the data collected for this particular HTTP
            // call.
            response.getInputStream().close();

            if (trace) {
                out.println("<-- " + locationHeader);
            }

            return locationHeader;
        } catch (IOException e) {
            throw sneakyThrow(e);
        }
    }

    private JSONObject getAndParseBodyAsJson(final URL url) {
        try {
            final HttpURLConnection response = sendRequest("GET", url, null,
                    false);

            final InputStream is;
            if ("gzip".equals(response.getContentEncoding())) {
                is = new GZIPInputStream(new BufferedInputStream(
                        response.getInputStream(), BUFSIZE));
            } else {
                is = new BufferedInputStream(response.getInputStream(),
                        BUFSIZE);
            }

            final ByteArrayOutputStream bytes = new ByteArrayOutputStream(
                    BUFSIZE);
            final byte[] buffer = new byte[BUFSIZE];
            int bytesRead = 0;
            do {
                bytesRead = is.read(buffer);
                if (bytesRead != -1) {
                    bytes.write(buffer, 0, bytesRead);
                }
            } while (bytesRead != -1);
            is.close();

            if (trace) {
                out.println("<-- " + bytes);
            }

            return (JSONObject) jsonParser.parse(new InputStreamReader(
                    new ByteArrayInputStream(bytes.toByteArray())));
        } catch (IOException e) {
            throw sneakyThrow(e);
        }

    }

    private static final int BUFSIZE = 16 * 1024; // 16k

    private HttpURLConnection sendRequest(final String method, final URL url,
            final String body, final boolean doOutput) throws IOException {
        final long start = currentTimeMillis();

        if (trace) {
            out.println(method + " " + url);
        }

        final HttpURLConnection connection = (HttpURLConnection) url
                .openConnection();
        connection.setRequestMethod(method);

        if (body != null) {
            connection.setRequestProperty("Content-Type",
                    "text/json; charset=utf-8");

            if (trace) {
                out.println("--> " + body);
            }
            final byte[] bytes = body.getBytes(UTF_8);
            connection.setDoOutput(doOutput);
            connection.setRequestProperty("Content-Length",
                    Integer.toString(bytes.length));
            connection.getOutputStream().write(bytes);
            connection.getOutputStream().flush();
        }

        if (trace) {
            out.println("<-- " + (currentTimeMillis() - start) + " ms HTTP "
                    + connection.getResponseCode() + ": "
                    + connection.getResponseMessage());
        }

        return connection;
    }
}
