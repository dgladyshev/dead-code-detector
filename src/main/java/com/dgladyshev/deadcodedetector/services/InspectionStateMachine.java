package com.dgladyshev.deadcodedetector.services;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.InspectionState;
import com.dgladyshev.deadcodedetector.repositories.InspectionsRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InspectionStateMachine {

    @Autowired
    private InspectionsRepository inspectionsRepository;

    public void changeState(Inspection inspection, InspectionState state) {
        inspection.setState(state);
        switch (state) {
            case ADDED:
                inspection.setTimestampInspectionCreated(System.currentTimeMillis());
                inspection.setStateDescription("Inspection created");
                break;
            case DOWNLOADING:
                inspection.setAntiPatternCodeOccurrences(null);
                inspection.setDeadCodeTypesFound(null);
                inspection.setTimeSpentAnalyzingMillis(null);
                inspection.setTimestampAnalysisStart(null);
                inspection.setTimestampAnalysisFinished(null);
                inspection.setStateDescription("Downloading git repository");
                break;
            case IN_QUEUE:
                inspection.setStateDescription("Request to analyze repository has been added to a queue");
                break;
            case PROCESSING:
                inspection.setTimestampAnalysisStart(System.currentTimeMillis());
                inspection.setStateDescription("Analyzing git repository and searching for dead code occurrences");
                break;
            case COMPLETED:
                inspection.setTimestampAnalysisFinished(System.currentTimeMillis());
                inspection.setTimeSpentAnalyzingMillis(
                        inspection.getTimestampAnalysisFinished() - inspection.getTimestampAnalysisStart()
                );
                inspection.setStateDescription("Processing completed");
                break;
            case FAILED:
                inspection.setTimestampAnalysisFinished(System.currentTimeMillis());
                break;
            default:
                break;
        }
        inspectionsRepository.save(inspection);
        log.info(
                "Inspection id: {}. State: {}. Description: {}",
                inspection.getId(),
                inspection.getState(),
                inspection.getStateDescription()
        );
    }

    public void complete(Inspection inspection, List<AntiPatternCodeOccurrence> antiPatternCodeOccurrences) {
        inspection.setAntiPatternCodeOccurrences(antiPatternCodeOccurrences);
        inspection.setDeadCodeTypesFound(
                antiPatternCodeOccurrences
                        .stream()
                        .map(AntiPatternCodeOccurrence::getType)
                        .distinct()
                        .sorted(String::compareTo)
                        .collect(Collectors.toList())
        );
        changeState(inspection, InspectionState.COMPLETED);
    }

    public void fail(Inspection inspection, Throwable ex) {
        log.error("Error occurred for inspection id: {}. Error: {}", inspection.getId(), ex);
        inspection.setStateDescription(ex.getMessage() + ". " + ex.getCause());
        changeState(inspection, InspectionState.FAILED);
    }

}


