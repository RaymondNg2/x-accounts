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

    public WebServer(final int port, final int backlog, final int poolsize,
            final API api) {
        super();

        this.port = port;
        this.backlog = backlog;
        this.poolsize = poolsize;
        this.api = api;
    }

    public void serve() throws IOException, InterruptedException {
        err.println("Spawning " + poolsize + " HTTP threads.");
        for (int i = 0; i < poolsize; i++) {
            new Thread(new ConnectionHandler(sockets, api),
                    "http-connection-" + i).start();
        }

        err.println("Binding to port " + port + ", backlog of " + backlog
                + " pending connections.");
        final ServerSocket serverSocker = new ServerSocket(port, backlog);
        for (;;) {
            final Socket socket = serverSocker.accept();
            err.println("Accepted new connection.");
            sockets.put(socket);
        }
    }
}
