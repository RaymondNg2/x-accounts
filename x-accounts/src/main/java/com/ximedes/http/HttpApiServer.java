package com.ximedes.http;

import static com.ximedes.http.UhmParser.uhmParseJsonLastInteger;
import static com.ximedes.http.UhmParser.uhmParseJsonTransfer;
import static com.ximedes.http.UhmParser.uhmParseUriLastInteger;
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

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.ximedes.API;
import com.ximedes.Account;
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

        // A lot of work only so that we can suppress sending the "Server"
        // header in the HTTP responses.
        final HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSendServerVersion(false);
        final HttpConnectionFactory httpFactory = new HttpConnectionFactory(
                httpConfiguration);
        final Server server = new Server();
        final ServerConnector serverConnector = new ServerConnector(server,
                httpFactory);
        serverConnector.setPort(8080);
        server.setConnectors(new Connector[] { serverConnector });
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

                handleGet(request, response);
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
        switch (request.getPathInfo().charAt(1)) {
        case 'a':
            final int overdraft = uhmParseJsonLastInteger(body);
            response.addHeader(LOCATION_HEADER,
                    "/account/" + api.createAccount(overdraft));

            response.setStatus(SC_ACCEPTED);
            break;
        case 't':
            final Transaction transfer = uhmParseJsonTransfer(body);
            final int transferId = api.transfer(transfer.from, transfer.to,
                    transfer.amount);
            response.addHeader(LOCATION_HEADER, "/transfer/" + transferId);

            response.setStatus(SC_ACCEPTED);
            break;
        default:
            response.setStatus(SC_NOT_FOUND);
        }
    }

    private void handleGet(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        // check charAt(1) since charAt(0) is a /
        switch (request.getPathInfo().charAt(1)) {
        case 'a':
            final int accountId = uhmParseUriLastInteger(request.getPathInfo());
            final Account account = api.getAccount(accountId);
            response.getWriter()
                    .write("{\"accountId\":\"" + account.accountId
                            + "\",\"balance\":" + account.balance
                            + ",\"overdraft\":" + account.overdraft + "}");
            response.setStatus(SC_OK);
            break;
        case 't':
            final int transferId = uhmParseUriLastInteger(
                    request.getPathInfo());
            final Transaction transfer = api.getTransfer(transferId);
            response.getWriter()
                    .write("{\"transactionId\":\"" + transfer.transactionId
                            + "\",\"from\":\"" + transfer.from + "\",\"to\":\""
                            + transfer.to + "\",\"amount\":" + transfer.amount
                            + ",\"status\":\"" + transfer.status + "\"}");
            response.setStatus(SC_OK);
            break;
        case 'p':
            api.ping();
            response.setStatus(SC_OK);
            break;
        case 'c':
            response.getWriter()
                    .write("{\"cents\":" + api.countCentsInTheSystem() + "}");
            response.setStatus(SC_OK);
            break;
        default:
            response.setStatus(SC_NOT_FOUND);
        }

    }
}
