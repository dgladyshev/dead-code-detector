package com.dgladyshev.deadcodedetector.entities;

public enum InspectionState {

    ADDED("ADDED"),
    DOWNLOADING("DOWNLOADING"),
    IN_QUEUE("IN_QUEUE"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");

    private String status;

    InspectionState(String status) {
        this.status = status;
    }

}
