package com.dgladyshev.deadcodedetector.services;

import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_BRANCH;
import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_LANGUAGE;
import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_REPO;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.InspectionState;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchGitBranchException;
import com.dgladyshev.deadcodedetector.util.InspectionUtils;
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
        Inspection inspection = InspectionUtils.createInspection(
                EXPECTED_REPO,
                EXPECTED_LANGUAGE,
                EXPECTED_BRANCH
        );
        ArrayList<AntiPatternCodeOccurrence> codeOccurrences = Lists.newArrayList();
        NoSuchGitBranchException exception = new NoSuchGitBranchException("Inspection failed");
        inspectionStateMachine.changeState(inspection, InspectionState.DOWNLOADING);
        inspectionStateMachine.changeState(inspection, InspectionState.IN_QUEUE);
        inspectionStateMachine.changeState(inspection, InspectionState.PROCESSING);
        inspectionStateMachine.complete(inspection, codeOccurrences);
        inspectionStateMachine.fail(inspection, exception);
    }

}