package com.ximedes.client;

import static com.mashape.unirest.http.Unirest.get;
import static com.mashape.unirest.http.Unirest.post;
import static com.ximedes.utils.SneakyThrows.sneakyThrow;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.ximedes.API;
import com.ximedes.Account;
import com.ximedes.Status;
import com.ximedes.Transaction;

/**
 * An HTTP API client.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
class HttpApiClient implements API {
    private static final int CUTOFF = 2500; /* milliseconds */

    private final String baseUrl;

    /**
     * Set up a new HTTP API client, specifying the base URL for the server to
     * connect to.
     * 
     * @param baseUrl
     *            The base URL (including trailing slash).
     * @param maxTotal
     *            Defines the overall connection limit for a connection pool.
     * @param maxPerRoute
     *            Defines a connection limit per one HTTP route (this can be
     *            considered a per target host limit).
     */
    public HttpApiClient(final String baseUrl, final int maxTotal,
            final int maxPerRoute) {
        super();

        this.baseUrl = baseUrl;
        Unirest.setConcurrency(maxTotal, maxPerRoute);

        out.println("Connecting to " + baseUrl);
        out.println("Max total " + maxTotal);
        out.println("Max per route " + maxPerRoute);
    }

    /**
     * @see com.ximedes.API#createAccount(int)
     */
    @Override
    public int createAccount(final int overdraft) {
        final long start = currentTimeMillis();

        final String json = "{\"overdraft\":" + overdraft + "}";
        try {
            final HttpResponse<JsonNode> response = post(baseUrl + "account")
                    .header("Content-Type", "application/json").body(json)
                    .asJson();

            final long responseTime = currentTimeMillis() - start;
            if (responseTime > CUTOFF) {
                out.println("create account took " + responseTime + " ms.");
            }

            return uhmParseUriLastInteger(
                    response.getHeaders().getFirst("Location"));
        } catch (UnirestException e) {
            throw sneakyThrow(e);
        }
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
        try {
            final HttpResponse<JsonNode> response = post(baseUrl + "transfer")
                    .header("Content-Type", "application/json").body(json)
                    .asJson();

            final long responseTime = currentTimeMillis() - start;
            if (responseTime > CUTOFF) {
                out.println("transfer took " + responseTime + " ms.");
            }

            return uhmParseUriLastInteger(
                    response.getHeaders().getFirst("Location"));
        } catch (UnirestException e) {
            throw sneakyThrow(e);
        }
    }

    /**
     * Chop the last bit off of an URI and use that as an integer value.
     * 
     * @param uri
     *            The URI to ... uhm ... parse.
     * @return The integer value of the last bit of the URI.
     */
    private int uhmParseUriLastInteger(final String uri) {
        final int lastSlash = uri.lastIndexOf('/');

        return parseInt(uri.substring(lastSlash + 1).trim());
    }

    /**
     * @see com.ximedes.API#countCentsInTheSystem()
     */
    @Override
    public int countCentsInTheSystem() {
        try {
            final HttpResponse<JsonNode> response = get(
                    baseUrl + "countCentsInTheSystem").asJson();

            return (int) response.getBody().getObject().get("cents");
        } catch (UnirestException e) {
            throw sneakyThrow(e);
        }
    }

    /**
     * @see com.ximedes.API#getAccount(int)
     */
    @Override
    public Account getAccount(final int accountId) {
        final long start = currentTimeMillis();

        try {
            final HttpResponse<JsonNode> response = get(
                    baseUrl + "account/" + accountId).asJson();

            final long responseTime = currentTimeMillis() - start;
            if (responseTime > CUTOFF) {
                out.println("GET transfer took " + responseTime + " ms.");
            }

            final JSONObject json = response.getBody().getObject();
            return new Account(accountId, json.getInt("balance"),
                    json.getInt("overdraft"));
        } catch (UnirestException e) {
            throw sneakyThrow(e);
        }
    }

    /**
     * @see com.ximedes.API#getTransfer(int)
     */
    @Override
    public Transaction getTransfer(final int transferId) {
        final long start = currentTimeMillis();

        try {
            final HttpResponse<JsonNode> response = get(
                    baseUrl + "transfer/" + transferId).asJson();

            final long responseTime = currentTimeMillis() - start;
            if (responseTime > CUTOFF) {
                out.println("GET transfer took " + responseTime + " ms.");
            }

            final JSONObject json = response.getBody().getObject();
            return new Transaction(transferId, json.getInt("from"),
                    json.getInt("to"), json.getInt("amount"),
                    json.optEnum(Status.class, "status"));
        } catch (UnirestException e) {
            throw sneakyThrow(e);
        }
    }

    /**
     * @see com.ximedes.API#ping()
     */
    @Override
    public void ping() {
        final long start = currentTimeMillis();

        try {
            get(baseUrl + "ping").asJson();

            final long responseTime = currentTimeMillis() - start;
            if (responseTime > CUTOFF) {
                out.println("ping took " + responseTime + " ms.");
            }
        } catch (UnirestException e) {
            throw sneakyThrow(e);
        }
    }
}
