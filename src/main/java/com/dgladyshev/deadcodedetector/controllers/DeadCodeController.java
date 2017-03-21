package com.dgladyshev.deadcodedetector.controllers;

import com.dgladyshev.deadcodedetector.entity.Check;
import com.dgladyshev.deadcodedetector.entity.CheckRequest;
import com.dgladyshev.deadcodedetector.services.CheckCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/v1/")
public class DeadCodeController {

	@Autowired
	CheckCodeService checkCodeService;

	@RequestMapping(value = "/checks", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity<Check> addCheck(@RequestBody CheckRequest checkRequest) {
		log.info("Incoming request: " + checkRequest);
		//TODO check that request is valid
		Check check = checkCodeService.createCheck(checkRequest.getUrl(), checkRequest.getLanguage());
		checkCodeService.checkCode(check.getCheckId());
		return new ResponseEntity<>(check, HttpStatus.OK);
	}

	@RequestMapping(value = "/checks", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity<Collection<Check>> getChecks() {
		return new ResponseEntity<>(
				checkCodeService.getChecks().values(),
				HttpStatus.OK
		);
	}

	@RequestMapping(value = "/checks/{id}", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity<Check> getCheckById(@PathVariable String id) {
		return new ResponseEntity<>(
				checkCodeService.getCheckById(id),
				HttpStatus.OK
		);
	}

}

