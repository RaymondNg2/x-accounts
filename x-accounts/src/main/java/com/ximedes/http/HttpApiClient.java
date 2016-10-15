package com.ximedes.http;

import static com.mashape.unirest.http.Unirest.post;
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
            if (responseTime > 20) {
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
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    /**
     * @see com.ximedes.API#countCentsInTheSystem()
     */
    @Override
    public int countCentsInTheSystem() {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

}
