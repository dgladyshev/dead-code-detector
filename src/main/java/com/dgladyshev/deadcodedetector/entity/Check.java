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
	private String repoUrl;
	private String repoName;
	private String repoLanguage;
	private CheckStatus checkStatus;
	private String stepDescription;
	private Long timeAdded;
	private Long timeCheckFinished;
	private List<DeadCodeOccurence> deadCodeOccurences;

}

