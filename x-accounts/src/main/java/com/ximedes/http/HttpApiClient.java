package com.ximedes.http;

import static com.mashape.unirest.http.Unirest.get;
import static com.mashape.unirest.http.Unirest.post;
import static com.ximedes.http.UhmParser.uhmParseUriLastInteger;
import static com.ximedes.utils.SneakyThrows.sneakyThrow;
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
public class HttpApiClient implements API {
    private final static String BASE_URL = "http://localhost:8080/";

    private static final int CUTOFF = 2500; /* milliseconds */

    static {
        Unirest.setConcurrency(110, 110);
    }

    /**
     * @see com.ximedes.API#createAccount(int)
     */
    @Override
    public int createAccount(final int overdraft) {
        final long start = currentTimeMillis();

        final String json = "{\"overdraft\":" + overdraft + "}";
        try {
            final HttpResponse<JsonNode> response = post(BASE_URL + "account")
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
            final HttpResponse<JsonNode> response = post(BASE_URL + "transfer")
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
     * @see com.ximedes.API#countCentsInTheSystem()
     */
    @Override
    public int countCentsInTheSystem() {
        try {
            final HttpResponse<JsonNode> response = get(
                    BASE_URL + "countCentsInTheSystem").asJson();

            return (int) response.getBody().getObject().get("cents");
        } catch (UnirestException e) {
            throw sneakyThrow(e);
        }
    }

    /**
     * @see com.ximedes.API#getAccount(int)
     */
    @Override
    public Account getAccount(int accountId) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    /**
     * @see com.ximedes.API#getTransfer(int)
     */
    @Override
    public Transaction getTransfer(int transferId) {

        final long start = currentTimeMillis();

        try {
            final HttpResponse<JsonNode> response = Unirest
                    .get(BASE_URL + "transfer/" + transferId).asJson();

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
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }
}
