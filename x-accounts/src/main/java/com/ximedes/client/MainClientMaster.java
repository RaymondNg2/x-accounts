package com.ximedes.client;

/**
 * The client's master main class.
 * This master creates the bank account and starts the slaves
 * Always start all the slaves, AppNodes and dbNode before the master
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class MainClientMaster {

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
		final ClientMaster clientMaster = new ClientMaster();
		clientMaster.fireXimedesTest();
	}
}
