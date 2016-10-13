package com.ximedes.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.ximedes.dto.Account;
import com.ximedes.dto.CreateAccount;
import com.ximedes.dto.TransAction;
import com.ximedes.dto.Transfer;

import java.io.*;
import java.util.*;

public class HttpClient {

	private final static String BASE_URL = "http://localhost/";

	static {
		// Only one time
		Unirest.setObjectMapper(new ObjectMapper() {
			private final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
					= new com.fasterxml.jackson.databind.ObjectMapper();

			public <T> T readValue(String value, Class<T> valueType) {
				try {
					return jacksonObjectMapper.readValue(value, valueType);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			public String writeValue(Object value) {
				try {
					return jacksonObjectMapper.writeValueAsString(value);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			}
		}
		);
	}

	/**
	 * Http POST to http://<>/account
	 * Body: {“overdraft”: 1000}
	 */
	public static String createAccount(int overdraft) throws UnirestException {
		HttpResponse<JsonNode> response = Unirest.post(BASE_URL + "account")
				.header("Content-Type", "application/json")
				.body(new CreateAccount(overdraft))
				.asJson();

		if (response.getStatus() == 202) {
			return response.getHeaders().getFirst("Location");
		} else if (response.getStatus() == 404) { // node offline => try again
			return createAccount(overdraft);
		}

		// you should never get here
		return null;
	}

	/**
	 * Retrieves the account
	 */
	public static Optional<Account> getAccount(String accountId) throws UnirestException {
		HttpResponse<Account> response = Unirest.get(BASE_URL + "account/{accountId}")
				.header("Content-Type", "application/json")
				.header("accept", "application/json")
				.routeParam("accountId", accountId)
				.asObject(Account.class);

		if (response.getStatus() == 200) {
			return Optional.of(response.getBody());
		} else if (response.getStatus() == 404) {
			return Optional.empty();
		}

		throw new RuntimeException("Response status should be 200 or 404 but is " + response.getStatus());
	}

	/**
	 * Http POST to http://<>/transfer
	 */
	public static String transferFunds(Transfer transfer) throws UnirestException {
		HttpResponse<JsonNode> response = Unirest.post(BASE_URL + "transfer")
				.header("Content-Type", "application/json")
				.body(transfer)
				.asJson();

		if (response.getStatus() == 202) {
			return response.getHeaders().getFirst("Location");
		} else if (response.getStatus() == 404) { // node offline => try again
			return transferFunds(transfer);
		}

		// you should never get here
		return null;
	}

	/**
	 * Retrieves transaction data. Needs to return a 200 within 60 seconds after creation
	 */
	public static Optional<TransAction> getTransaction(String transactionId) throws UnirestException {

		HttpResponse<TransAction> response = Unirest.get(BASE_URL + "transaction/{transactionId}")
				.header("Content-Type", "application/json")
				.header("accept", "application/json")
				.routeParam("transactionId", transactionId)
				.asObject(TransAction.class);

		if (response.getStatus() == 200) {
			return Optional.of(response.getBody());
		} else if (response.getStatus() == 404) {
			return Optional.empty();
		}

		throw new RuntimeException("Response status should be 200 or 404 but is " + response.getStatus());
	}
}
