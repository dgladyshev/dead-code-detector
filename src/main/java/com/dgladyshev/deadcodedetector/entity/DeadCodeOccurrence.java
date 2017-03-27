package com.dgladyshev.deadcodedetector.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class DeadCodeOccurrence {

    private String type;
    private String name;
    private String file;
    private String line;
    private String column;

}

