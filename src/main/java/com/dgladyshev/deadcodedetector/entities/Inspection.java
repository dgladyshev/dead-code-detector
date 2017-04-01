package com.dgladyshev.deadcodedetector.entities;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.beans.BeanUtils;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
@Entity
@DynamicUpdate
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private GitRepo gitRepo;
    private String url;
    private String language;
    private String branch;
    @Enumerated(EnumType.STRING)
    private InspectionState state;
    @Lob
    @Column(length = 1000)
    private String stateDescription;
    private Long timestampInspectionCreated;
    private Long timestampAnalysisFinished;
    private Long timestampAnalysisStart;
    private Long timeSpentAnalyzingMillis;
    @ElementCollection
    private List<String> deadCodeTypesFound;
    @ElementCollection(fetch = FetchType.LAZY)
    private List<AntiPatternCodeOccurrence> antiPatternCodeOccurrences;

    public Inspection(GitRepo gitRepo, String language, String branch, String url) {
        this.gitRepo = gitRepo;
        this.language = language;
        this.branch = branch;
        this.url = url;
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

