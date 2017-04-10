package com.dgladyshev.deadcodedetector.services;

import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_BRANCH;
import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_LANGUAGE;
import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_REPO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.InspectionState;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchGitBranchException;
import java.util.ArrayList;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class InspectionStateMachineTest {

    @Autowired
    InspectionStateMachine inspectionStateMachine;

    @MockBean
    InspectionsService inspectionsService;

    @Test
    public void changeState() throws Exception {
        Inspection inspection = Inspection.buildInspection(
                EXPECTED_REPO,
                EXPECTED_LANGUAGE,
                EXPECTED_BRANCH
        );
        final ArrayList<AntiPatternCodeOccurrence> codeOccurrences = Lists.newArrayList(
                new AntiPatternCodeOccurrence()
        );
        final String errorDescription = "Unique description of error";
        final NoSuchGitBranchException exception = new NoSuchGitBranchException(errorDescription);

        inspectionStateMachine.changeState(inspection, InspectionState.DOWNLOADING);
        assertEquals(inspection.getState(), InspectionState.DOWNLOADING);
        assertEquals(inspection.getAntiPatternCodeOccurrences(), null);
        assertEquals(inspection.getDeadCodeTypesFound(), null);
        assertEquals(inspection.getTimeSpentAnalyzingMillis(), null);
        assertEquals(inspection.getTimestampAnalysisStart(), null);
        assertEquals(inspection.getTimestampAnalysisFinished(), null);

        inspectionStateMachine.changeState(inspection, InspectionState.IN_QUEUE);
        assertEquals(inspection.getState(), InspectionState.IN_QUEUE);

        inspectionStateMachine.changeState(inspection, InspectionState.PROCESSING);
        assertEquals(inspection.getState(), InspectionState.PROCESSING);
        assertNotNull(inspection.getTimestampAnalysisStart());

        inspectionStateMachine.complete(inspection, codeOccurrences);
        assertEquals(inspection.getState(), InspectionState.COMPLETED);
        assertEquals(inspection.getAntiPatternCodeOccurrences(), codeOccurrences);
        assertNotNull(inspection.getTimestampAnalysisFinished());
        assertNotNull(inspection.getTimeSpentAnalyzingMillis());

        inspectionStateMachine.fail(inspection, exception);
        assertEquals(inspection.getState(), InspectionState.FAILED);
        assertNotNull(inspection.getTimestampAnalysisFinished());
        assertTrue(inspection.getStateDescription().contains(errorDescription));
    }

}