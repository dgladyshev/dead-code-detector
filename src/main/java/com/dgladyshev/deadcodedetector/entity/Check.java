package com.dgladyshev.deadcodedetector.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class Check {

	private String checkId;
	private String repoUrl;
	private String repoLanguage;
	private CheckStatus checkStatus;
	private String pathToGraph; //TODO rename
	private Long timeAdded;
	private Long timeCheckFinished;
	private Object checkResult; //TODO understand what to use here

}

