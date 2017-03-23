package com.dgladyshev.deadcodedetector.controllers;

import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.entity.SupportedLanguages;
import com.dgladyshev.deadcodedetector.services.InspectionService;
import com.dgladyshev.deadcodedetector.util.GitHubRepositoryName;
import com.dgladyshev.deadcodedetector.util.URLChecker;
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

	private InspectionService inspectionService;

	@Autowired
	public DeadCodeController(InspectionService inspectionService) {
		this.inspectionService = inspectionService;
	}

	@RequestMapping(value = "/inspections", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity<Inspection> addInspection(@RequestParam String url, @RequestParam SupportedLanguages language) {
		log.info("Incoming request for analysis, url: {}, language: {}", url, language);
		GitRepo gitRepo = toGitRepo(url, language.getName());
		URLChecker.isAccessible(url);
		Inspection inspection = inspectionService.createInspection(gitRepo);
		inspectionService.inspectCode(inspection.getInspectionId());
		return new ResponseEntity<>(inspection, HttpStatus.OK);
	}

	@RequestMapping(value = "/inspections", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity<Collection<Inspection>> getInspections() {
		return new ResponseEntity<>(
				inspectionService.getInspections().values(),
				HttpStatus.OK
		);
	}

	@RequestMapping(value = "/inspections/{id}", method = RequestMethod.GET)
	public
	@ResponseBody
	Inspection getInspectionById(@PathVariable String id) {
		return inspectionService.getInspection(id);
	}

	@RequestMapping(value = "/inspections/{id}", method = RequestMethod.DELETE)
	public
	@ResponseBody
	void deleteInspectionById(@PathVariable String id) {
		inspectionService.deleteInspection(id);
	}

	private GitRepo toGitRepo(@RequestParam String url, @RequestParam String language) {
		GitHubRepositoryName parsedURL = GitHubRepositoryName.create(url);
		return GitRepo.builder()
				.host(parsedURL.getHost())
				.name(parsedURL.getRepositoryName())
				.user(parsedURL.getUserName())
				.url(url)
				.language(language)
				.build();
	}

}

