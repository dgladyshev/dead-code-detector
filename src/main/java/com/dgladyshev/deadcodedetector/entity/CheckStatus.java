package com.dgladyshev.deadcodedetector.entity;

public enum CheckStatus {

	ADDED("ADDED"),
	PROCESSING("PROCESSING"),
	COMPLETED("COMPLETED"),
	FAILED("FAILED");

	private String status;

	CheckStatus(String status) {
		this.status = status;
	}

	public String status() {
		return status;
	}

}
