package com.dgladyshev.deadcodedetector.controllers;

import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.services.InspectionService;
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
	InspectionService inspectionService;

	@RequestMapping(value = "/inspections", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity<Inspection> addInspection(@RequestBody GitRepo gitRepo) {
		log.info("Incoming request: " + gitRepo);
		//TODO check repo has right naming AND is accessible
		//TODO add name to repo
		//TODO check that language is supported and convert it to lowercase
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

}

