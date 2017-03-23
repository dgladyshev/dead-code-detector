package com.dgladyshev.deadcodedetector.entity;

public enum InspectionStatus {

	ADDED("ADDED"),
	PROCESSING("PROCESSING"),
	COMPLETED("COMPLETED"),
	FAILED("FAILED");

	private String status;

	InspectionStatus(String status) {
		this.status = status;
	}

}
