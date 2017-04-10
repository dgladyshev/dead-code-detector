package com.dgladyshev.deadcodedetector.services;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.InspectionState;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InspectionStateMachine {

    @Autowired
    private InspectionsService inspectionsService;

    public void changeState(Inspection inspection, InspectionState state) {
        changeState(inspection, state, null);
    }

    private void changeState(Inspection inspection, InspectionState state, Object argument) {
        inspection.setState(state);
        inspection.setStateDescription(state.getDescription());
        switch (state) {
            case DOWNLOADING:
                inspection.setAntiPatternCodeOccurrences(null);
                inspection.setDeadCodeTypesFound(null);
                inspection.setTimeSpentAnalyzingMillis(null);
                inspection.setTimestampAnalysisStart(null);
                inspection.setTimestampAnalysisFinished(null);
                break;
            case IN_QUEUE:
                break;
            case PROCESSING:
                inspection.setTimestampAnalysisStart(System.currentTimeMillis());
                break;
            case COMPLETED:
                inspection.setTimestampAnalysisFinished(System.currentTimeMillis());
                inspection.setTimeSpentAnalyzingMillis(
                        inspection.getTimestampAnalysisFinished() - inspection.getTimestampAnalysisStart()
                );
                List<AntiPatternCodeOccurrence> codeOccurrences = (List<AntiPatternCodeOccurrence>) argument;
                inspection.setAntiPatternCodeOccurrences(codeOccurrences);
                inspection.setDeadCodeTypesFound(
                        codeOccurrences
                                .stream()
                                .map(AntiPatternCodeOccurrence::getType)
                                .distinct()
                                .sorted(String::compareTo)
                                .collect(Collectors.toList())
                );
                break;
            case FAILED:
                inspection.setTimestampAnalysisFinished(System.currentTimeMillis());
                Throwable ex = (Throwable) argument;
                log.error("Error occurred for inspection id: {}. Error: {}", inspection.getId(), ex);
                String stateDescription = ex.getMessage() + ". " + ex.getCause();
                inspection.setStateDescription(stateDescription);
                break;
            default:
                break;
        }
        inspectionsService.saveInspection(inspection);
    }

    public void complete(Inspection inspection, List<AntiPatternCodeOccurrence> antiPatternCodeOccurrences) {
        changeState(inspection, InspectionState.COMPLETED, antiPatternCodeOccurrences);
    }

    public void fail(Inspection inspection, Throwable ex) {
        changeState(inspection, InspectionState.FAILED, ex);
    }

}


