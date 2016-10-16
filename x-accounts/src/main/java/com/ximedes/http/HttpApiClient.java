package com.ximedes.http;

import static com.mashape.unirest.http.Unirest.get;
import static com.mashape.unirest.http.Unirest.post;
import static com.ximedes.Status.CONFIRMED;
import static com.ximedes.http.UhmParser.uhmParseUriLastInteger;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.ximedes.API;
import com.ximedes.Transaction;

/**
 * An HTTP API client.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class HttpApiClient implements API {
    private final static String BASE_URL = "http://localhost:8080/";

    private static final int CUTOFF = 25; /* milliseconds */

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
            throw new RuntimeException(e);
        }
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
        try {
            final HttpResponse<JsonNode> response = post(BASE_URL + "transfer")
                    .header("Content-Type", "application/json").body(json)
                    .asJson();

            final long responseTime = currentTimeMillis() - start;
            if (responseTime > CUTOFF) {
                out.println("transfer took " + responseTime + " ms.");
            }

            // XXX WRONG The transaction status is not actually known at this
            // point. So we need to change the test to fetch the transaction
            // status after the fact.
            return new Transaction(
                    uhmParseUriLastInteger(
                            response.getHeaders().getFirst("Location")),
                    from, to, amount, CONFIRMED);
        } catch (UnirestException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }
}
