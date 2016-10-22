package com.ximedes.webserver;

import static java.lang.System.err;
import static java.lang.System.exit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import com.ximedes.API;
import com.ximedes.Account;
import com.ximedes.Transaction;

/**
 * The handler that handles a stream of HTTP requests from a socket.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
class ConnectionHandler implements Runnable {
    private static final int READ_BUFSIZE = 512; // bytes

    private final BlockingQueue<Socket> sockets;
    private final API api;

    private static final boolean trace = false;

    public ConnectionHandler(final BlockingQueue<Socket> sockets,
            final API api) {
        super();

        this.sockets = sockets;
        this.api = api;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            for (;;) {
                final Socket socket = sockets.take();
                final InputStream is = socket.getInputStream();
                final OutputStream os = socket.getOutputStream();

                for (;;) {
                    final byte[] httpRequest = new byte[READ_BUFSIZE];
                    int requestSize = readRequest(is, httpRequest);
                    if (requestSize == -1) {
                        if (trace) {
                            err.println("Stream closed normally.");
                        }
                        is.close();
                        os.close();
                        break;
                    } else if (isPOST(httpRequest)) {
                        requestSize = readBody(is, httpRequest, requestSize);
                        final String response = handlePOST(httpRequest,
                                requestSize);
                        writeResponse(response, os);
                    } else {
                        final String response = handleGET(httpRequest,
                                requestSize);
                        writeResponse(response, os);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            err.println("Exiting...");
            err.flush();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                // ignore, we are going down anyway.
            }
            exit(1); // XXX Rude...
        } finally {
            err.println("Exiting ConnectionHandler.run...");
        }
    }

    /**
     * Read a single HTTP request from the socket. As optimisation, I make the
     * rather nasty assumption that all HTTP request headers arrive in a single
     * <code>read(buf)</code>. If this assumption holds, which is realistic
     * because we are dealing with tiny HTTP requests, we can save a lot of time
     * by not dealing with HTTP request assembly.
     */
    private int readRequest(final InputStream is, final byte[] httpRequest)
            throws IOException {
        final int requestSize = is.read(httpRequest);

        if (trace && requestSize != -1) {
            err.println("read, size is now " + requestSize + " ["
                    + new String(httpRequest, 0, requestSize) + "]");
        }

        return requestSize;
    }

    /**
     * Determine if an HTTP request calls the POST method, or some other method.
     * We simply check the first letter of the request. If that is a capital P,
     * this request is likely a call to HTTP POST. Likely enough for our
     * purposes.
     * 
     * @return <true> if this is an HTTP POST, or <false> if it is not. Most
     *         likely an HTTP GET, but we don't check.
     */
    private boolean isPOST(final byte[] httpRequest) {
        return httpRequest[0] == 'P';
    }

    /**
     * Optionally read the HTTP request body from the stream. I have seen two
     * cases: one where headers and body arrive in the same HTTP packet, and one
     * where they arrive in separate packets. So the <code>read(buf)</code> we
     * did earlier may or may not have sucked in the HTTP request body.
     * <p>
     * This method checks if there is a body present. If it finds no HTTP
     * request body, it will execute an additional <code>read(buf)</code> and
     * append the HTTP request body to the set of headers.
     * 
     * @throws IOException
     */
    private int readBody(final InputStream is, final byte[] httpRequest,
            int requestSize) throws IOException {
        if (httpRequest[requestSize - 1] == '\n') {
            requestSize += is.read(httpRequest, requestSize,
                    READ_BUFSIZE - requestSize);
            if (trace) {
                err.println("  -> extra read, size is now " + requestSize + " ["
                        + new String(httpRequest, 0, requestSize) + "]");
            }
        }
        return requestSize;
    }

    /**
     * Handle an HTTP POST request. We use the same crude byte indexing to
     * determine what the path is that is being posted to. Then we parse the
     * body, in style.
     * 
     * <pre>
     *   POST /[a]ccount HTTP/1.1
     *   POST /[t]ransfer HTTP/1.1
     * </pre>
     * 
     * @return The HTTP response text.
     */
    private String handlePOST(final byte[] httpRequest, final int requestSize) {
        final String path;
        final int id;
        switch (httpRequest[6]) {
        case 'a':
            path = "/account/";
            id = createAccount(httpRequest, requestSize);
            break;
        case 't':
            path = "/transfer/";
            id = transfer(httpRequest, requestSize);
            break;
        default:
            throw new IllegalArgumentException(
                    "Found bad path " + (char) httpRequest[6] + " on ["
                            + new String(httpRequest, 0, requestSize) + "]");
        }

        return "HTTP/1.1 202 ACCEPTED\nLocation: " + path + id
                + "\nContent-Length: 0\n\n";
    }

    /**
     * Parse the overdraft from the HTTP request body and create an account with
     * the specified overdraft. The overdraft value is the last numeric value in
     * the HTTP request, so it is pretty easy to find of we start at the back
     * and work our way forward.
     */
    private int createAccount(final byte[] httpRequest, final int requestSize) {
        int overdraft = 0;

        // skip over all non-numeric stuff, like the trailing brace and any
        // whitespace there.
        int currentChar = requestSize - 1;
        while (httpRequest[currentChar] < '0'
                || httpRequest[currentChar] > '9') {
            currentChar--;
        }

        // now read all digits and accumulate those into an overdraft value.
        int factor = 1;
        while (httpRequest[currentChar] >= '0'
                && httpRequest[currentChar] <= '9') {
            overdraft += ((httpRequest[currentChar] - '0') * factor);
            factor *= 10;
            currentChar--;
        }

        return api.createAccount(overdraft);
    }

    private int transfer(final byte[] httpRequest, final int requestSize) {
        int from = -1;
        int to = -1;
        int amount = -1;

        int currentChar = requestSize - 1;
        for (int i = 0; i < 3; i++) {
            int value = 0;

            // skip over all non-numeric stuff, like the trailing brace and any
            // whitespace there.
            while (httpRequest[currentChar] < '0'
                    || httpRequest[currentChar] > '9') {
                currentChar--;
            }

            // now read all digits and accumulate those into a value.
            int factor = 1;
            while (httpRequest[currentChar] >= '0'
                    && httpRequest[currentChar] <= '9') {
                value += ((httpRequest[currentChar] - '0') * factor);
                factor *= 10;
                currentChar--;
            }

            // again skip over stuff until we see one of the letters signifying
            // a field name. We use the last letter of the field name to
            // identify what field we have, fro[m], t[o] or amoun[t].
            while (httpRequest[currentChar] != 'm'
                    && httpRequest[currentChar] != 'o'
                    && httpRequest[currentChar] != 't') {
                currentChar--;
            }

            switch (httpRequest[currentChar]) {
            case 'm':
                from = value;
                break;
            case 'o':
                to = value;
                break;
            case 't':
                amount = value;
                break;
            default:
                throw new IllegalStateException(
                        "bad identifier " + httpRequest[currentChar]);
            }
        }

        return api.transfer(from, to, amount);
    }

    /**
     * Handle the HTTP GET requests. This time we don't have to parse the body.
     * We can simply use the path to find out what is being requested. Again, we
     * use a single character to identify which path is being used.
     * 
     * <pre>
     *   GET /[a]ccount/457 HTTP/1.1
     *   GET /[t]ransfer/115637 HTTP/1.1
     *   GET /[p]ing HTTP/1.1
     *   GET /[c]ountCentsInTheSystem HTTP/1.1
     *   GET /[h]ealth HTTP/1.1
     * </pre>
     * 
     * We then optionally grab the identifier from the path.
     */
    private String handleGET(final byte[] httpRequest, final int requestSize) {
        final StringBuilder response = new StringBuilder("");
        switch (httpRequest[5]) {
        case 'a':
            final int accountId = idFromPath(httpRequest);
            final Account account = api.getAccount(accountId);
            response.append("{\"accountId\":\"").append(account.accountId)
                    .append("\",\"balance\":").append(account.balance)
                    .append(",\"overdraft\":").append(account.overdraft)
                    .append("}");
            break;
        case 't':
            final int transferId = idFromPath(httpRequest);
            final Transaction transfer = api.getTransfer(transferId);
            response.append("{\"transactionId\":\"")
                    .append(transfer.transactionId).append("\",\"from\":\"")
                    .append(transfer.from).append("\",\"to\":\"")
                    .append(transfer.to).append("\",\"amount\":")
                    .append(transfer.amount).append(",\"status\":\"")
                    .append(transfer.status).append("\"}");
            break;
        case 'c':
            response.append("{\"cents\":").append(api.countCentsInTheSystem())
                    .append('}');
            break;
        case 'p':
        case 'h':
            // no body to return...
            break;
        default:
            throw new IllegalArgumentException(
                    "Found bad path " + (char) httpRequest[5] + " on ["
                            + new String(httpRequest, 0, requestSize) + "]");
        }

        final int contentLength = response.length();
        response.insert(0, "\n\n").insert(0, contentLength).insert(0,
                "HTTP/1.1 200 OK\nContent-Length: ");
        return response.toString();
    }

    /**
     * Grab the identifier from a path in an HTTP GET request.
     */
    private int idFromPath(final byte[] httpRequest) {
        int currentChar;
        switch (httpRequest[5]) {
        case 'a':
            currentChar = 13;
            break;
        case 't':
            currentChar = 14;
            break;
        default:
            throw new IllegalArgumentException(
                    "Found bad path " + (char) httpRequest[5] + " on ["
                            + new String(httpRequest) + "]");

        }

        int value = 0;
        while (httpRequest[currentChar] >= '0'
                && httpRequest[currentChar] <= '9') {
            value *= 10;
            value += (httpRequest[currentChar] - '0');
            currentChar++;
        }
        return value;
    }

    private void writeResponse(final String response, final OutputStream os)
            throws IOException {
        if (trace) {
            err.println(response);
        }

        os.write(response.getBytes());
        os.flush();
    }

    /**
     * Reader and ... uhm ... parse the headers of the HTTP request. We could
     * parse the headers properly, but they are completely irrelevant to our
     * application. We can safely ignore the lot of them.
     * <p>
     * What we do need is to know what HTTP method is being called and which
     * path it is being called on. We can do that quite quickly by using the
     * first letter of the first HTTP header line, plus the 7th letter on that
     * same line. Those two uniquely identify the caller's intent. This is shown
     * below with brackets around the 1st and 7nd letters.
     * 
     * 
     */
}
