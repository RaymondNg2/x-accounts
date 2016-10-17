package com.ximedes.http;

import static com.ximedes.http.UhmParser.uhmParseJsonLastInteger;
import static java.lang.System.err;
import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.ximedes.API;
import com.ximedes.Simpleton;
import com.ximedes.Transaction;

/**
 * The server-side HTTP wrapper.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class HttpApiServer extends AbstractHandler {
    private static final String LOCATION_HEADER = "Location";

    private final API api = new Simpleton();

    private static final boolean trace = false;

    /**
     * Create and start a new web server on port 8080.
     * 
     * @throws Exception
     *             When the web server failed to start.
     */
    public HttpApiServer() throws Exception {
        super();

        final Server server = new Server(8080);
        server.setHandler(this);

        server.start();
    }

    /**
     * @see AbstractHandler#handle(String, Request, HttpServletRequest,
     *      HttpServletResponse)
     */
    @Override
    public void handle(final String target, final Request baseRequest,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws IOException, ServletException {
        try {
            if (request.getMethod().charAt(0) == 'P' /* POST */) {
                final StringBuilder body = readBody(request);
                if (trace) {
                    err.println(request.getMethod() + " "
                            + request.getPathInfo() + " -> " + body);
                }

                handlePost(request, body, response);
            } else if (request.getMethod().charAt(0) == 'G' /* GET */) {
                if (trace) {
                    err.println(
                            request.getMethod() + " " + request.getPathInfo());
                }

                handleGet(response);
            } else {
                err.println("method not allowed: " + request.getMethod() + " "
                        + request.getPathInfo());
                response.setStatus(SC_METHOD_NOT_ALLOWED);
            }

            baseRequest.setHandled(true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    private StringBuilder readBody(final HttpServletRequest request)
            throws IOException {
        final StringBuilder body = new StringBuilder();
        String line;
        final BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body;
    }

    private void handlePost(final HttpServletRequest request,
            final StringBuilder body, final HttpServletResponse response) {
        // check charAt(1) since charAt(0) is a /
        if (request.getPathInfo().charAt(1) == 'a') {
            final int overdraft = uhmParseJsonLastInteger(body);
            response.addHeader(LOCATION_HEADER,
                    "/account/" + api.createAccount(overdraft));

            response.setStatus(SC_ACCEPTED);
        } else if (request.getPathInfo().charAt(1) == 't') {
            final Transaction transfer = UhmParser.uhmParseJsonTransfer(body);
            final Transaction transaction = api.transfer(transfer.from,
                    transfer.to, transfer.amount);
            response.addHeader(LOCATION_HEADER,
                    "/transfer/" + transaction.transactionId);

            response.setStatus(SC_ACCEPTED);
        } else {
            response.setStatus(SC_NOT_FOUND);
        }
    }

    private void handleGet(final HttpServletResponse response)
            throws IOException {
        response.getWriter()
                .write("{\"cents\":" + api.countCentsInTheSystem() + "}");

        response.setStatus(SC_OK);
    }
}
