package com.dgladyshev.deadcodedetector.entities;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.beans.BeanUtils;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
@NodeEntity
public class Inspection {

    @GraphId
    private Long id;
    @Relationship(type = "INSPECT_IN", direction = Relationship.UNDIRECTED)
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
    @Relationship(type = "OCCUR_IN", direction = Relationship.UNDIRECTED)
    private List<AntiPatternCodeOccurrence> antiPatternCodeOccurrences;

    public Inspection(GitRepo gitRepo, String language, String branch) {
        this.gitRepo = gitRepo;
        this.language = language;
        this.branch = branch;
    }

    //returns filtered representation of inspection
    //warning: method creates new instance of inspection class
    public Inspection toFilteredInspection(String filter) {
        if (this.getAntiPatternCodeOccurrences() != null) {
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

}

