package com.dgladyshev.deadcodedetector.controllers.constants;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;

public class ControllerExpectedEntities {

    public static final String EXPECTED_ID = "some-unique-id";
    public static final String EXPECTED_REPO_URL = "https://github.com/dgladyshev/dead-code-detector.git";
    public static final String EXPECTED_LANGUAGE = "JAVA";
    public static final String EXPECTED_BRANCH = "master";
    public static final GitRepo EXPECTED_REPO = new GitRepo(EXPECTED_REPO_URL);
    public static final Inspection EXPECTED_INSPECTION = Inspection
            .builder()
            .id(EXPECTED_ID)
            .url(EXPECTED_REPO_URL)
            .language(EXPECTED_LANGUAGE)
            .branch(EXPECTED_BRANCH)
            .gitRepo(EXPECTED_REPO)
            .build();

}
