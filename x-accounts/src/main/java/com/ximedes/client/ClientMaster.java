package com.ximedes.client;

import com.hazelcast.core.IMap;
import com.ximedes.API;

import static java.lang.System.out;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;

public class ClientMaster {

	private static final boolean trace = false;
	private static final int amountTestingNodes = 1;

	private final API api = new HttpApiClient("http://127.0.0.1:8080/",
											  110, 110);
	private final IMap<Integer, Integer> startCommand;
	private final IMap<String, Integer> slavesFinished;

	public ClientMaster() throws UnknownHostException {
		startCommand = HazelcastConfig.hazelcastInstance().getMap("startCommand");
		slavesFinished = HazelcastConfig.hazelcastInstance().getMap("slavesFinished");
	}

	public void fireXimedesTest() {
		// log the start time
		final LocalDateTime start = LocalDateTime.now();
		out.println("== Test started at " + start.toString());

		// create the bank account
		final int backAccountId = api.createAccount(30000000);
		if (trace) {
			out.println("created bankAccount " + backAccountId);
		}

		// send start command with the bankId
		sendStartMessageToSlaves(backAccountId);

		// wait for all nodes to be finished
		listenForSlaves();

		final LocalDateTime end = LocalDateTime.now();
		out.println("== Test ended at " + end.toString());
		out.println("== Test duration " + Duration.between(start, end).getSeconds() + " seconds");
	}

	private void sendStartMessageToSlaves(int backAccountId) {
		startCommand.put(1, backAccountId);
	}

	private void listenForSlaves() {
		try {
			while (slavesFinished.size() < amountTestingNodes) {
				Thread.sleep(500);
			}
		} catch (InterruptedException ex) {
			out.println(ex);
		}
	}
}
