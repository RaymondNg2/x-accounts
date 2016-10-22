package com.ximedes.client;

import static java.lang.System.out;
import java.util.*;

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
		// Some system properties are useful to tweak the network stack. This is
		// relevant for the HTTPUrlConnection class, but does not hurt for any
		// of the other code.
		//
		// The only relevant tweaking I found make a different is
		// http.maxConnections. That reduces the number of actual network
		// connections being created and helps avoid spurious
		// "java.net.SocketException: Invalid argument" messages.
		//
		// Best start the JVM with:
		// -Djava.net.preferIPv4Stack=true
		// -Dhttp.keepalive=true
		// -Dhttp.maxConnections=110
		final Map<Object, Object> properties = System.getProperties();
		for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
			final String key = (String)entry.getKey();
			if (key.startsWith("http.") || key.startsWith("java.net.")) {
				out.println(entry.getKey() + "=" + entry.getValue());
			}
		}

		final ClientMaster clientMaster = new ClientMaster();
		clientMaster.fireXimedesTest();
	}
}
