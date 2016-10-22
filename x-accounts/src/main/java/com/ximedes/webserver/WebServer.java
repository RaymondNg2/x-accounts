package com.ximedes.webserver;

import static java.lang.System.err;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ximedes.API;

/**
 * A web server that is dedicated to handling this challenge's traffic. The
 * assumption here is that by cutting every conveivable corner of the HTTP
 * protocol, we can build a faster web server than the professionals can.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class WebServer {
    private final int port;
    private final int backlog;
    private final int poolsize;
    private final API api;
    private final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();

    /**
     * Set up a new web server.
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
     */
    public WebServer(final int port, final int backlog, final int poolsize,
            final API api) {
        super();

        this.port = port;
        this.backlog = backlog;
        this.poolsize = poolsize;
        this.api = api;
    }

    /**
     * Serve HTTP requests. This method blocks and only returns when a
     * catastrophic exception is being thrown.
     * 
     * @throws IOException
     *             When something happened and the server is inoperable.
     * @throws InterruptedException
     *             When the server was interrupted.
     */
    public void serve() throws IOException, InterruptedException {
        err.println("Spawning " + poolsize + " HTTP threads.");
        for (int i = 0; i < poolsize; i++) {
            new Thread(new ConnectionHandler(sockets, api),
                    "http-connection-" + i).start();
        }

        err.println("Binding to port " + port + ", backlog of " + backlog
                + " pending connections.");
        try (final ServerSocket serverSocker = new ServerSocket(port,
                backlog)) {
            for (;;) {
                sockets.put(serverSocker.accept());
            }
        }
    }
}
