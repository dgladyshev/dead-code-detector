package com.dgladyshev.deadcodedetector.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Data
@Builder
@Slf4j
public class Inspection {

    private String inspectionId;
    private GitRepo gitRepo;
    private InspectionStatus status;
    private String stepDescription;
    private Long timestampAdded;
    private Long timestampFinished;
    private Long timestampDownloaded;
    private Long timeSpentAnalyzing;
    private List<DeadCodeOccurence> deadCodeOccurrences;


    //TODO create state machine logic here

    public void startProcessing() {
        this.setStatus(InspectionStatus.PROCESSING);
        this.setStepDescription("Step 1/5. Downloading git repository");
    }

    public void repoDownloaded() {
        this.setTimestampDownloaded(System.currentTimeMillis());
        this.setStepDescription("Step 2/5. Request to analyze repository has been added to a queue");
    }

    public void analyzeRepository() {
        this.setStepDescription("Step 3/5. Analyzing git repository and creating .udb file");
    }

    public void inspectRepository() {
        this.setStepDescription("Step 4/5. Searching for dead code occurrences");
    }

    public void completeInspection(List<DeadCodeOccurence> deadCodeOccurrences) {
        this.setDeadCodeOccurrences(deadCodeOccurrences);
        this.setTimestampFinished(System.currentTimeMillis());
        this.setTimeSpentAnalyzing(this.getTimestampFinished() - this.getTimestampDownloaded());
        this.setStepDescription("Step 5/5. Processing completed");
        this.setStatus(InspectionStatus.COMPLETED);
    }

    public void fail(Exception ex) {
        log.error("Error occurred for inspection id: {}. Error: {}", this.inspectionId, ex);
        this.setStatus(InspectionStatus.FAILED);
    }
}

