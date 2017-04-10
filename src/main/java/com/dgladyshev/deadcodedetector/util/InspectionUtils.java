package com.dgladyshev.deadcodedetector.util;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.InspectionState;
import java.util.UUID;

public class InspectionUtils {

    public static Inspection createInspection(GitRepo repo, String language, String branch) {
        return Inspection.builder()
                .id(UUID.randomUUID().toString())
                .gitRepo(repo)
                .timestampInspectionCreated(System.currentTimeMillis())
                .state(InspectionState.ADDED)
                .stateDescription("Inspection created")
                .language(language)
                .branch(branch)
                .build();
    }

}
