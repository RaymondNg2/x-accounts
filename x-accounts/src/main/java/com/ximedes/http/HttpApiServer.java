package com.ximedes.http;

import static java.lang.System.err;
import static java.lang.System.exit;

import java.io.IOException;

import com.ximedes.API;
import com.ximedes.Simpleton;
import com.ximedes.webserver.WebServer;

/**
 * The server-side HTTP wrapper.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class HttpApiServer {
    private final API api = new Simpleton();
    private final WebServer webServer = new WebServer(8080, 1024, 250, api);

    /**
     * Create and start a new web server on port 8080.
     * 
     * @throws Exception
     *             When the web server failed to start.
     */
    public HttpApiServer() throws Exception {
        super();

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
                    }
                    exit(1);
                } finally {
                    err.println("Exiting HttpApiServer.run...");
                }
            }
        }).start();
    }
}
