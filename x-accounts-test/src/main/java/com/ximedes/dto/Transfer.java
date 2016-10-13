package com.ximedes.dto;

import lombok.Data;

@Data
public class Transfer {

	private String from;
	private String to;
	int amount;
}
