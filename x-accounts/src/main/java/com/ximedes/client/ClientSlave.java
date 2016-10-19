package com.ximedes.client;

import com.ximedes.API;
import com.ximedes.http.HttpUrlConnectionApiClient;

import java.io.*;
import static java.lang.System.exit;
import static java.lang.System.out;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ClientSlave {

	private static final boolean trace = false;

	private final API api = new HttpUrlConnectionApiClient();

	private final InetAddress multicastAddressStartCommand;
	private final InetAddress multicastAddressFinished;
	private final int multicastPort = 24625;

	private final AtomicInteger amountScenariosFinished = new AtomicInteger();
	private int bankAccount;

	public ClientSlave() throws UnknownHostException {
		this.multicastAddressStartCommand = InetAddress.getByName("232.57.108.73");
		this.multicastAddressFinished = InetAddress.getByName("232.57.108.74");
	}

	public void startSlave() throws InterruptedException {
		// wait for start command
		listenForStartCommand();

		// spawn 10 thread to run the below
		final Thread[] machines = new Thread[10];
		// 10 machines each run ...
		for (int machine = 0; machine < 10; machine++) {
			if (trace) {
				out.println("started machine thread " + machine);
			}

			machines[machine] = new Thread(() -> {
				try {
					runScenario();
				} catch (InterruptedException e) {
					e.printStackTrace();
					exit(1);
				}
			});
			machines[machine].start();
		}
		for (int machine = 0; machine < 10; machine++) {
			machines[machine].join();
		}

		// send done to ClientMaster
		sendFinishedMessageToMaster();
	}

	private void runScenario() throws InterruptedException {
		for (; amountScenariosFinished.get() < 30; amountScenariosFinished.incrementAndGet()) {
			if (trace) {
				out.println("Started with Scenario # " + amountScenariosFinished.get());
			}

			// create merchant account
			final int merchantAccount = api.createAccount(0);
			if (trace) {
				out.println("Created merchantAccount " + merchantAccount);
			}

			// create 1000 consumer accounts
			final List<Integer> consumerAccounts = create1000ConsumerAccounts();
			if (trace) {
				out.println("Created 1000 consumerAccounts " + merchantAccount);
			}

			// create 10 threads
			final Thread[] transferThread = new Thread[10];
			for (int thread = 0; thread < 10; thread++) {
				transferThread[thread] = new Thread(new TransferThread(merchantAccount, consumerAccounts.subList(100 * thread, 100 * thread + 99)));
				transferThread[thread].start();
			}
			for (int machine = 0; machine < 10; machine++) {
				transferThread[machine].join();
			}
			/* merchant should have 10000 cents */
			// method not yet in api to check the balance of an account
		}
	}

	public class TransferThread implements Runnable {

		private final int merchantAccount;

		private final List<Integer> consumerAccounts;
		private final List<Integer> transfers;
		private final List<ToTransfer> toTransfers = new ArrayList<>();

		public TransferThread(int merchantAccount, List<Integer> consumerAccounts) {
			this.consumerAccounts = consumerAccounts;
			this.merchantAccount = merchantAccount;
			transfers = new ArrayList<>();

			consumerAccounts.forEach(consumerAccount -> toTransfers.add(new ToTransfer(consumerAccount)));
		}

		@Override
		public void run() {
			if (trace) {
				out.println("transfer 10 cents to all consumerAccounts");
			}
			// transfer 10 cents from bank account to 100 consumers
			consumerAccounts.forEach(consumerAccount -> {
				final int transactionId = api.transfer(bankAccount, consumerAccount, 10);
				transfers.add(transactionId);
			});

			/* All 1000 consumers now have 10 cents */
			// in random order 12 transfers are made from each consumer to the merchant
			if (trace) {
				out.println("in random order 12 transfers are made from each consumer to the merchant");
			}
			while (!toTransfers.isEmpty()) {
				// toTransfer.size() is ok because it is exclusive
				int nextIndex = ThreadLocalRandom.current().nextInt(0, toTransfers.size());
				ToTransfer toTransfer = toTransfers.get(nextIndex);
				api.transfer(toTransfer.consumerAccount, merchantAccount, 1);
				toTransfer.toTransferRemaining--;

				if (toTransfer.toTransferRemaining == 0) {
					toTransfers.remove(nextIndex);
				}
			}
		}

		private class ToTransfer {

			public int consumerAccount;
			public int toTransferRemaining;

			public ToTransfer(int consumerAccount) {
				this.consumerAccount = consumerAccount;
				this.toTransferRemaining = 12;
			}
		}
	}

	private List<Integer> create1000ConsumerAccounts() {
		final List<Integer> consumerAccounts = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			final int consumerAccountId = api.createAccount(0);
			consumerAccounts.add(consumerAccountId);
		}

		return consumerAccounts;
	}

	private void listenForStartCommand() {
		byte[] buf = new byte[256];

		try (MulticastSocket clientSocket = new MulticastSocket(multicastPort)) {
			clientSocket.joinGroup(multicastAddressStartCommand);

			if (trace) {
				out.println("ClientSlave started to listen for startcommand on " + multicastAddressStartCommand.getHostAddress() + ":" + multicastPort);
			}

			DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
			// clientSocket.receive is blocking
			clientSocket.receive(msgPacket);

			bankAccount = ByteBuffer.wrap(buf).getInt();
			if (trace) {
				out.println("Received BankAccount " + bankAccount);
			}

			clientSocket.leaveGroup(multicastAddressStartCommand);
			clientSocket.close();
		} catch (IOException ex) {
			out.println("ERROR: " + ex.getMessage());
		}
	}

	private void sendFinishedMessageToMaster() {
		byte[] msg = "done".getBytes();

		// Open a new DatagramSocket, which will be used to send the data.
		try (DatagramSocket serverSocket = new DatagramSocket()) {
			// Create a packet that will contain the data
			// (in the form of bytes) and send it.
			DatagramPacket msgPacket = new DatagramPacket(msg,
														  msg.length,
														  multicastAddressFinished,
														  multicastPort);
			if (trace) {
				out.println("ClientSlave sends finished message to ClientMaster on multicast address " + multicastAddressFinished.getHostAddress() + ":" + multicastPort);
			}

			serverSocket.send(msgPacket);
			serverSocket.close();
		} catch (IOException ex) {
			// what should be done when this is catched?
			// for now output it
			out.println("ERROR: " + ex.getMessage());
		}
	}
}
