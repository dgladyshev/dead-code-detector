package com.dgladyshev.deadcodedetector.services;

import com.dgladyshev.deadcodedetector.repositories.InspectionsRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SuppressWarnings("PMD.UnusedPrivateField")
public class CodeAnalyzerServiceIntegrationTest {

    @Autowired
    private CodeAnalyzerService codeAnalyzerService;

    @Autowired
    private InspectionStateMachine inspectionStateMachine;

    @Autowired
    private InspectionsService inspectionsService;

    @Autowired
    private InspectionsRepository inspectionsRepository;

    @Autowired
    private GitService gitService;

    @Test //TODO fix
    public void inspectCode() throws Exception {
        //        String url = "https://github.com/dgladyshev/sampleGradleProject";
        //        GitRepo gitRepo = new GitRepo(url);
        //        Inspection initialInspection = inspectionsService.createInspection(
        //                gitRepo,
        //                SupportedLanguages.JAVA.toString(),
        //                "master",
        //                url
        //        ).block();
        //        codeAnalyzerService.inspectCode(initialInspection);
        //TODO fix tests with get request to controller
        //        assertNotEquals(null, processedInspection);
        //        assertNotEquals(null, processedInspection.getAntiPatternCodeOccurrences());
        //        assertNotEquals(null, processedInspection.getDeadCodeTypesFound());
        //        assertEquals(6, processedInspection.getAntiPatternCodeOccurrences().size());
    }

}