package com.dgladyshev.deadcodedetector.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class Check {

	private String checkId;
	private GitRepo gitRepo;
	private CheckStatus status;
	private String stepDescription;
	private Long timestampAdded;
	private Long timestampFinished;
	private Long timeSpentMillis;
	private List<DeadCodeOccurence> deadCodeOccurences;

}

