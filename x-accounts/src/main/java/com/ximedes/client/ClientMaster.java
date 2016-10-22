package com.ximedes.client;

import static java.lang.System.out;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;

import com.ximedes.API;
import com.ximedes.http.HttpApiClient;

public class ClientMaster {

	private static final boolean trace = false;
	private static final int amountTestingNodes = 1;

	private final API api = new HttpApiClient();
	private final InetAddress multicastAddressStartCommand;
	private final InetAddress multicastAddressListen;
	private final int multicastPort = 24625;

	public ClientMaster() throws UnknownHostException {
		this.multicastAddressStartCommand = InetAddress.getByName("232.57.108.73");
		this.multicastAddressListen = InetAddress.getByName("232.57.108.74");
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
		byte[] msg = ByteBuffer.allocate(4).putInt(backAccountId).array();

		// Open a new DatagramSocket, which will be used to send the data.
		try (DatagramSocket serverSocket = new DatagramSocket()) {
			// Create a packet that will contain the data
			// (in the form of bytes) and send it.
			DatagramPacket msgPacket = new DatagramPacket(msg,
														  msg.length,
														  multicastAddressStartCommand,
														  multicastPort);
			if (trace) {
				out.println("ClientMaster sends start message to ClientSlaves on multicast address " + multicastAddressStartCommand.getHostAddress() + ":" + multicastPort);
			}

			serverSocket.send(msgPacket);
			serverSocket.close();
		} catch (IOException ex) {
			// what should be done when this is catched?
			// for now output it
			out.println("ERROR: " + ex.getMessage());
		}
	}

	private void listenForSlaves() {
		byte[] buf = new byte[256];

		try (MulticastSocket clientSocket = new MulticastSocket(multicastPort)) {
			clientSocket.joinGroup(multicastAddressListen);

			if (trace) {
				out.println("ClientMaster started to listen for slaves on " + multicastAddressListen.getHostAddress() + ":" + multicastPort);
			}
			for (int i = 0; i < amountTestingNodes; i++) {
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				// clientSocket.receive is blocking
				clientSocket.receive(msgPacket);

				if (trace) {
					String msg = new String(buf, 0, buf.length);
					out.println("ClientSlave is done: " + msg);
				}
			}

			clientSocket.leaveGroup(multicastAddressListen);
			clientSocket.close();
		} catch (IOException ex) {
			out.println("ERROR: " + ex.getMessage());
		}
	}
}
