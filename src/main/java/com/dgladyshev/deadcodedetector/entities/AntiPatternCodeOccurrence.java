package com.dgladyshev.deadcodedetector.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@NodeEntity
public class AntiPatternCodeOccurrence {

    @GraphId
    private Long id;

    private AntiPatternType antiPatternType;
    private String type;
    private String name;
    private String file;
    private Integer line;
    private Integer column;

}

