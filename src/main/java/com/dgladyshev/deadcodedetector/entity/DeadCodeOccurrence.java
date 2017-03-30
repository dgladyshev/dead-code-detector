package com.dgladyshev.deadcodedetector.entity;

import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Embeddable
public class DeadCodeOccurrence {

    private String type;
    private String name;
    private String file;
    private String line;
    private String column;

}

