package com.dgladyshev.deadcodedetector.entity;

import lombok.Data;

@Data
public class CheckRequest {

	private String url;
	private String language;

}
