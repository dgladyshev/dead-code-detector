package com.dgladyshev.deadcodedetector.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class Inspection {

    private String inspectionId;
    private GitRepo gitRepo;
    private InspectionStatus status;
    private String stepDescription;
    private Long timestampAdded;
    private Long timestampFinished;
    private Long timeSpentMillis;
    private List<DeadCodeOccurence> deadCodeOccurrences;

}

