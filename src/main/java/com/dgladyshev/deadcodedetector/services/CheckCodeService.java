package com.dgladyshev.deadcodedetector.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CheckCodeService {

	@Async
	public void checkCode(String checkId, String url, String language) {
		//TODO implement
	}

}
