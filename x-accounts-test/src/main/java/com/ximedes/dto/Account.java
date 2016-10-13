package com.ximedes.dto;

import lombok.Data;

@Data
public class Account {

	private String accountId;
	private Integer balance;
	private Integer overdraft;
}
