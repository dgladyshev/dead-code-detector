package com.dgladyshev.deadcodedetector.controllers;

import com.dgladyshev.deadcodedetector.entity.CheckRequest;
import com.dgladyshev.deadcodedetector.entity.CheckStatus;
import com.dgladyshev.deadcodedetector.entity.RepositoryCheckStatus;
import com.dgladyshev.deadcodedetector.services.CheckCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class DeadCodeController {

	@Autowired
	CheckCodeService checkCodeService;

	@RequestMapping(value = {"/health"}, method = RequestMethod.GET)
	public String healthCheck() {
		//TODO implement smart health check
		return "OK";
	}

	@RequestMapping(value = "api/v1/repositories", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity<RepositoryCheckStatus> addCheckRequest(@RequestBody CheckRequest checkRequest) {
		log.info("Incoming request: " + checkRequest);
		//TODO check that request is valid
		//TODO generate id for repository check
		String checkId = java.util.UUID.randomUUID().toString(); //TODO generate unique id
		//TODO process request async
		checkCodeService.checkCode(checkId, checkRequest.getUrl(), checkRequest.getLanguage());
		return new ResponseEntity<>(new RepositoryCheckStatus(checkId, CheckStatus.ADDED), HttpStatus.OK);
	}

}

