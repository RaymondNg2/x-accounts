package com.ximedes.dto;

import lombok.Data;

@Data
public class TransAction {

	private String transactionId;
	private String from;
	private String to;
	private Integer amount;
	private String status;
}
