package com.ximedes.client;

/**
 * The client's slave main class.
 *
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class MainClientSlave {

	/**
	 * Start the HTTP server to serve the API requests for the challenge.
	 *
	 * @param args
	 *             Ignored.
	 *
	 * @throws Exception
	 *                   When there was an error starting the server.
	 */
	public static void main(final String[] args) throws Exception {
		final ClientSlave clientSlave = new ClientSlave();
		clientSlave.startSlave();
	}
}
