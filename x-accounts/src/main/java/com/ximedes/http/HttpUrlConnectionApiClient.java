package com.ximedes.http;

import static com.ximedes.http.UhmParser.uhmParseJsonAccount;
import static com.ximedes.http.UhmParser.uhmParseJsonLastInteger;
import static com.ximedes.http.UhmParser.uhmParseJsonTransfer;
import static com.ximedes.http.UhmParser.uhmParseUriLastInteger;
import static com.ximedes.utils.SneakyThrows.sneakyThrow;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.ximedes.API;
import com.ximedes.Account;
import com.ximedes.Transaction;

/**
 * An HTTP API client based on Java's HTTPUrlConnection.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class HttpUrlConnectionApiClient implements API {
    private static final String urlBase = "http://127.0.0.1:8080/";
    private final URL pingUrl;
    private static final String accountUrlBase = urlBase + "account/";
    private final URL accountUrl;
    private static final String transferUrlBase = urlBase + "transfer/";
    private final URL transferUrl;
    private final String countUrl = urlBase + "countCentsInTheSystem";

    /**
     * Create the URLs. We use both strings and URL objects to avoid creating
     * lots of identical URL objects for the POST requests. For the GET requests
     * we still have to create URLs on the fly.
     */
    public HttpUrlConnectionApiClient() {
        try {
            pingUrl = new URL(urlBase + "ping");
            accountUrl = new URL(urlBase + "account");
            transferUrl = new URL(urlBase + "transfer");
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
     * @see com.ximedes.API#getAccount(int)
     */
    @Override
    public Account getAccount(final int accountId) {
        final StringBuilder json = getAndReadBody(accountUrlBase + accountId);
        return uhmParseJsonAccount(json);
    }

    /**
     * @see com.ximedes.API#transfer(int, int, int)
     */
    @Override
    public int transfer(final int from, final int to, final int amount) {
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

        return uhmParseUriLastInteger(locationHeader);
    }

    /**
     * @see com.ximedes.API#getTransfer(int)
     */
    @Override
    public Transaction getTransfer(final int transferId) {
        final StringBuilder json = getAndReadBody(transferUrlBase + transferId);
        return uhmParseJsonTransfer(json);
    }

    // --- the non-standard methods are below

    /**
     * @see com.ximedes.API#ping()
     */
    @Override
    public void ping() {
        postAndReturnLocationHeader(pingUrl, null);
    }

    /**
     * @see com.ximedes.API#countCentsInTheSystem()
     */
    @Override
    public int countCentsInTheSystem() {
        final StringBuilder json = getAndReadBody(countUrl);
        return uhmParseJsonLastInteger(json);
    }

    // --- the HTTP client implementation is below

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

    private StringBuilder getAndReadBody(final String url) {
        try {
            final HttpURLConnection response = sendRequest("GET", new URL(url),
                    null, false);

            final BufferedReader is = new BufferedReader(
                    new InputStreamReader(response.getInputStream()));
            final StringBuilder body = new StringBuilder();
            String line = null;
            while ((line = is.readLine()) != null) {
                body.append(line);
            }

            is.close();

            if (trace) {
                out.println("<-- " + body);
            }

            return body;
        } catch (IOException e) {
            throw sneakyThrow(e);
        }
    }

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
