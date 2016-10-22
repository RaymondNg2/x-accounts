package com.ximedes.server;

import static java.lang.System.err;
import static java.lang.System.exit;

import java.io.IOException;

import com.ximedes.API;

/**
 * The server-side HTTP wrapper.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
class HttpApiServer {
    /**
     * Create and start a new web server on port 8080.
     * 
     * @param port
     *            The port to listed on.
     * @param backlog
     *            The backlog to use from the application, note that the
     *            effective value is determined by a combination of this value
     *            and the <code>ulimit(1)</code. settings for the running user.
     * @param poolsize
     *            The number of HTTP handling threads to spawn.
     * @param api
     *            The API to call when a request comes in and was parsed.
     * @throws Exception
     *             When the web server failed to start.
     */
    public HttpApiServer(final int port, final int backlog, final int poolsize,
            final API api) throws Exception {
        super();

        final WebServer webServer = new WebServer(port, backlog, poolsize, api);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    webServer.serve();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    err.println("Dying...");
                    err.flush();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        // ignore, we are going down anyway.
                    }
                    exit(1);
                } finally {
                    err.println("Exiting HttpApiServer.run...");
                }
            }
        }).start();
    }
}
