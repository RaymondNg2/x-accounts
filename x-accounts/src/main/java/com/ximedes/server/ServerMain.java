package com.ximedes.server;

import static java.lang.Long.MAX_VALUE;
import static java.lang.System.err;

import com.ximedes.API;
import com.ximedes.Simpleton;

/**
 * The server's main class.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class ServerMain {
    /**
     * Start the HTTP server to serve the API requests for the challenge.
     * 
     * @param args
     *            Ignored.
     * @throws Exception
     *             When there was an error starting the server.
     */
    public static void main(final String[] args) throws Exception {
        final API api = new Simpleton();
        @SuppressWarnings("unused")
        final HttpApiServer httpApiServer = new HttpApiServer(8080, 1024, 150,
                api);

        Thread.sleep(MAX_VALUE);
        err.println("Done sleeping...");
    }
}
