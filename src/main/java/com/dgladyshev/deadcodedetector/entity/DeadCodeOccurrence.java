package com.dgladyshev.deadcodedetector.entity;

import java.io.Serializable;
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
public class DeadCodeOccurrence implements Serializable {

    private String type;
    private String name;
    private String file;
    private String line;
    private String column;

}

