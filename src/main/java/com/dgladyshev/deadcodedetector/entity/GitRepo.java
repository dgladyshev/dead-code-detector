package com.dgladyshev.deadcodedetector.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class GitRepo {

	private String name;
	private String user;
	private String host;
	private String url;
	private String language;

}
