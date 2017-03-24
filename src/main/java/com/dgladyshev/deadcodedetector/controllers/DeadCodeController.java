package com.dgladyshev.deadcodedetector.controllers;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.entity.SupportedLanguages;
import com.dgladyshev.deadcodedetector.repositories.InspectionsRepository;
import com.dgladyshev.deadcodedetector.services.InspectionService;
import com.dgladyshev.deadcodedetector.services.UrlCheckerService;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/")
public class DeadCodeController {

    private final InspectionService inspectionService;
    private final InspectionsRepository inspectionsRepository;
    private final UrlCheckerService urlCheckerService;

    @Autowired
    public DeadCodeController(InspectionService inspectionService,
                              InspectionsRepository inspectionsRepository,
                              UrlCheckerService urlCheckerService) {
        this.inspectionService = inspectionService;
        this.inspectionsRepository = inspectionsRepository;
        this.urlCheckerService = urlCheckerService;
    }

    @RequestMapping(value = "/inspections", method = RequestMethod.POST)
    @ResponseBody
    public Inspection addInspection(@RequestParam String url,
                                    @RequestParam SupportedLanguages language,
                                    @RequestParam(defaultValue = "master") String branch) {
        log.info("Incoming request for analysis, url: {}, language: {}", url, language);
        String trimmedUrl = trimToEmpty(url);
        String trimmedBranch = trimToEmpty(branch);
        GitRepo gitRepo = new GitRepo(trimmedUrl, language.getName(), trimmedBranch);
        urlCheckerService.checkAccessibility(trimmedUrl.replace(".git", ""));
        Inspection inspection = inspectionsRepository.createInspection(gitRepo);
        inspectionService.inspectCode(inspection.getInspectionId());
        return inspection;
    }

    @RequestMapping(value = "/inspections", method = RequestMethod.GET)
    @ResponseBody
    public Collection<Inspection> getInspections() {
        return inspectionsRepository.getInspections().values();
    }

    @RequestMapping(value = "/inspections/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Inspection getInspectionById(@PathVariable String id) {
        return inspectionsRepository.getInspection(id);
    }

    @RequestMapping(value = "/inspections/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteInspectionById(@PathVariable String id) {
        inspectionsRepository.deleteInspection(id);
    }

}

