package com.dgladyshev.deadcodedetector.controllers.constants;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.InspectionState;

public class ControllerExpectedEntities {

    public static final String EXPECTED_ID = "some-unique-id";
    public static final String EXPECTED_REPO_URL = "https://github.com/dgladyshev/dead-code-detector.git";
    public static final String EXPECTED_LANGUAGE = "java";
    public static final String EXPECTED_BRANCH = "master";
    public static final GitRepo EXPECTED_REPO = new GitRepo(EXPECTED_REPO_URL);
    public static final Inspection EXPECTED_INSPECTION = Inspection
            .builder()
            .id(EXPECTED_ID)
            .language(EXPECTED_LANGUAGE)
            .branch(EXPECTED_BRANCH)
            .gitRepo(EXPECTED_REPO)
            .state(InspectionState.FAILED)
            .build();

}
