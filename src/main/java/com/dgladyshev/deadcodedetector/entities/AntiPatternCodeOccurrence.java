package com.dgladyshev.deadcodedetector.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AntiPatternCodeOccurrence {

    private AntiPatternType antiPatternType;
    private String type;
    private String name;
    private String file;
    private Integer line;
    private Integer column;

}

