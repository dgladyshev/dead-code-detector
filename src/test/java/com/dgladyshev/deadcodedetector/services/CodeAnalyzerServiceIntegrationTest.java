package com.dgladyshev.deadcodedetector.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.SupportedLanguages;
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

    @Test
    public void inspectCode() throws Exception {
        String url = "https://github.com/dgladyshev/sampleGradleProject";
        GitRepo gitRepo = new GitRepo(url);
        Inspection initialInspection = inspectionsService.createInspection(
                gitRepo,
                SupportedLanguages.JAVA.toString(),
                "master",
                url
        );
        Inspection processedInspection = codeAnalyzerService.inspectCode(initialInspection.getId());
        assertNotEquals(null, processedInspection);
        assertNotEquals(null, processedInspection.getAntiPatternCodeOccurrences());
        assertNotEquals(null, processedInspection.getDeadCodeTypesFound());
        assertEquals(6, processedInspection.getAntiPatternCodeOccurrences().size());
    }

}