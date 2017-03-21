package com.dgladyshev.deadcodedetector.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RepositoryCheckStatus {

	private String checkId;
	private CheckStatus checkStatus;

}
