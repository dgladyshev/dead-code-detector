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
    private InspectionState state;
    private String stateDescription;
    private Long timestampInspectionCreated;
    private Long timestampAnalysisFinished;
    private Long timestampAnalysisStart;
    private Long timeSpentAnalyzingMillis;
    private List<DeadCodeOccurence> deadCodeOccurrences;

    public void changeState(InspectionState state) {
        this.setState(state);
        switch (state) {
            case DOWNLOADING:
                this.setStateDescription("Downloading git repository");
                break;
            case IN_QUEUE:
                this.setStateDescription("Request to analyze repository has been added to a queue");
                break;
            case PROCESSING:
                this.setTimestampAnalysisStart(System.currentTimeMillis());
                this.setStateDescription("Analyzing git repository and searching for dead code occurrences");
                break;
            case COMPLETED:
                this.setTimestampAnalysisFinished(System.currentTimeMillis());
                this.setTimeSpentAnalyzingMillis(this.getTimestampAnalysisFinished() - this.getTimestampAnalysisStart());
                this.setStateDescription("Processing completed");
                break;
            case FAILED:
                this.setTimestampAnalysisFinished(System.currentTimeMillis());
                break;
            default:
                break;
        }
    }

    public void complete(List<DeadCodeOccurence> deadCodeOccurrences) {
        this.setDeadCodeOccurrences(deadCodeOccurrences);
        changeState(InspectionState.COMPLETED);
    }

    public void fail(Exception ex) {
        log.error("Error occurred for inspection id: {}. Error: {}", this.inspectionId, ex);
        this.setStateDescription(ex.getMessage());
        changeState(InspectionState.FAILED);
    }
}

