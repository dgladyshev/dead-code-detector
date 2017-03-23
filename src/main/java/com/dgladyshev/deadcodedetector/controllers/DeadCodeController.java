package com.dgladyshev.deadcodedetector.controllers;

import com.dgladyshev.deadcodedetector.entity.Check;
import com.dgladyshev.deadcodedetector.entity.GitRepo;
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
	ResponseEntity<Check> addCheck(@RequestBody GitRepo gitRepo) {
		log.info("Incoming request: " + gitRepo);
		//TODO check that request is valid
		//TODO add name to repo
		Check check = checkCodeService.createCheck(gitRepo);
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
	Check getCheckById(@PathVariable String id) {
		return checkCodeService.getCheck(id);
	}

	@RequestMapping(value = "/checks/{id}", method = RequestMethod.DELETE)
	public
	@ResponseBody
	void deleteCheckById(@PathVariable String id) {
		checkCodeService.deleteCheck(id);
	}

}

