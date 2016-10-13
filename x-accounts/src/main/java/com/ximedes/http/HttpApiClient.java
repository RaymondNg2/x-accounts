package com.ximedes.http;

import static io.vertx.core.Vertx.vertx;

import com.ximedes.API;
import com.ximedes.Transaction;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

/**
 * An HTTP API client.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class HttpApiClient implements API {
    private final HttpClientOptions options = new HttpClientOptions()
            .setDefaultHost("127.0.0.1").setDefaultPort(8080)
            .setLogActivity(true);
    private final HttpClient client = vertx().createHttpClient(options);

    /**
     * @see com.ximedes.API#createAccount(int)
     */
    @Override
    public int createAccount(final int overdraft) {
        final String json = "{\"overdraft\":" + overdraft + "}";
        client.post("/account", response -> {
            System.out.println("Received response with status code "
                    + response.statusCode());
        }).putHeader("content-length", Integer.toString(json.length()))
                .putHeader("content-type", "application/json").write(json)
                .end();
        return 12; // XXX wrong
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
