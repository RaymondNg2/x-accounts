package com.ximedes.http;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;

/**
 * The server-side HTTP wrapper.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class HttpApiServer implements Handler<HttpServerRequest> {

    /**
     * @see io.vertx.core.Handler#handle(java.lang.Object)
     */
    @Override
    public void handle(final HttpServerRequest event) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }
}
