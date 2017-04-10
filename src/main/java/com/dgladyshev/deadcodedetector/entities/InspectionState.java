package com.dgladyshev.deadcodedetector.entities;

public enum InspectionState {

    ADDED("ADDED", "Inspection created"),
    DOWNLOADING("DOWNLOADING", "Downloading git repository"),
    IN_QUEUE("IN_QUEUE", "Request to analyze repository has been added to a queue"),
    PROCESSING("PROCESSING", "Analyzing git repository and searching for dead code occurrences"),
    COMPLETED("COMPLETED", "Processing completed"),
    FAILED("FAILED", "Inspection failed");

    private String state;

    private String description;

    InspectionState(String state, String description) {
        this.state = state;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
