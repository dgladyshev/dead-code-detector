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

	@RequestMapping(value = "/checks", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity<Inspection> addCheck(@RequestBody GitRepo gitRepo) {
		log.info("Incoming request: " + gitRepo);
		//TODO inspection that request is valid
		//TODO add name to repo
		Inspection inspection = inspectionService.createInspection(gitRepo);
		inspectionService.inspectCode(inspection.getInspectionId());
		return new ResponseEntity<>(inspection, HttpStatus.OK);
	}

	@RequestMapping(value = "/checks", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity<Collection<Inspection>> getChecks() {
		return new ResponseEntity<>(
				inspectionService.getInspections().values(),
				HttpStatus.OK
		);
	}

	@RequestMapping(value = "/checks/{id}", method = RequestMethod.GET)
	public
	@ResponseBody
	Inspection getCheckById(@PathVariable String id) {
		return inspectionService.getInspection(id);
	}

	@RequestMapping(value = "/checks/{id}", method = RequestMethod.DELETE)
	public
	@ResponseBody
	void deleteCheckById(@PathVariable String id) {
		inspectionService.deleteInspection(id);
	}

}

