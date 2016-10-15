package com.ximedes.http;

import static com.ximedes.http.UhmParser.uhmParseJsonLastInteger;
import static java.lang.System.err;
import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
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

/**
 * The server-side HTTP wrapper.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class HttpApiServer extends AbstractHandler {
    private final API api = new Simpleton();

    private final boolean trace = true;

    public HttpApiServer() throws Exception {
        super();

        Server server = new Server(8080);
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
                response.setStatus(SC_ACCEPTED);
            } else if (request.getMethod().charAt(0) == 'G' /* GET */) {
                if (trace) {
                    err.println(
                            request.getMethod() + " " + request.getPathInfo());
                }

                handleGet();
                response.setStatus(SC_OK);
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
        final int overdraft = uhmParseJsonLastInteger(body);
        response.addHeader("Location",
                "/account/" + api.createAccount(overdraft));
    }

    private void handleGet() {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }
}
