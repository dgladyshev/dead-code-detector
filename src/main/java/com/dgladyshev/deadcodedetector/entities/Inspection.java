package com.dgladyshev.deadcodedetector.entities;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import io.netty.util.internal.StringUtil;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
@Document
public class Inspection {

    @Id
    private String id;
    private GitRepo gitRepo;
    private String language;
    private String branch;
    private InspectionState state;
    private String stateDescription;
    private Long timestampInspectionCreated;
    private Long timestampAnalysisFinished;
    private Long timestampAnalysisStart;
    private Long timeSpentAnalyzingMillis;
    private List<String> deadCodeTypesFound;
    private List<AntiPatternCodeOccurrence> antiPatternCodeOccurrences;

    public Inspection(GitRepo gitRepo, String language, String branch) {
        this.id = UUID.randomUUID().toString();
        this.gitRepo = gitRepo;
        this.language = language;
        this.branch = branch;
    }

    //returns filtered representation of inspection
    //warning: method creates new instance of inspection class
    public Inspection toFilteredInspection(String filter) {
        if (StringUtil.isNullOrEmpty(trimToEmpty(filter)) && this.getAntiPatternCodeOccurrences() != null) {
            List<AntiPatternCodeOccurrence> filteredOccurrences = this.getAntiPatternCodeOccurrences()
                    .stream()
                    .filter(occurrence -> {
                        String type = occurrence.getType().toLowerCase();
                        return type.contains(filter.toLowerCase());
                    })
                    .collect(Collectors.toList());
            Inspection filteredInspection = BeanUtils.instantiateClass(Inspection.class);
            BeanUtils.copyProperties(this, filteredInspection);
            filteredInspection.setAntiPatternCodeOccurrences(filteredOccurrences);
            return filteredInspection;
        } else {
            return this;
        }
    }

    public static Inspection buildInspection(GitRepo repo, String language, String branch) {
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

