package com.dgladyshev.deadcodedetector.entities;

public enum AntiPatternType {

    DEAD_CODE("DEAD_CODE");

    private String name;

    AntiPatternType(String name) {
        this.name = name.toLowerCase();
    }

    public String getName() {
        return name;
    }
}
