package com.dgladyshev.deadcodedetector.entity;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@SuppressWarnings("PMD.NullAssignment")
@Slf4j
@Entity
//@DynamicUpdate
public class Inspection {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private GitRepo gitRepo;
    private String url;
    private String language;
    private String branch;
    @Enumerated(EnumType.STRING)
    private InspectionState state;
    private String stateDescription;
    private Long timestampInspectionCreated;
    private Long timestampAnalysisFinished;
    private Long timestampAnalysisStart;
    private Long timeSpentAnalyzingMillis;
    @ElementCollection
    private List<String> deadCodeTypesFound;
    @ElementCollection
    private List<DeadCodeOccurrence> deadCodeOccurrences;

    public Inspection(GitRepo gitRepo, String language, String branch, String url) {
        this.gitRepo = gitRepo;
        this.language = language;
        this.branch = branch;
        this.url = url;
    }

    //returns filtered representation of inspection
    //warning: method creates new instance of inspection class
    public Inspection toFilteredInspection(String filter) {
        if (this.getDeadCodeOccurrences() != null) {
            List<DeadCodeOccurrence> filteredOccurrences = this.getDeadCodeOccurrences()
                    .stream()
                    .filter(occurrence -> {
                        String type = occurrence.getType().toLowerCase();
                        return type.contains(filter.toLowerCase());
                    })
                    .collect(Collectors.toList());
            Inspection filteredInspection = BeanUtils.instantiateClass(Inspection.class);
            BeanUtils.copyProperties(this, filteredInspection);
            filteredInspection.setDeadCodeOccurrences(filteredOccurrences);
            return filteredInspection;
        } else {
            return this;
        }
    }

}

