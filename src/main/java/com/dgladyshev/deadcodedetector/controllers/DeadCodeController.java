package com.dgladyshev.deadcodedetector.controllers;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.entity.SupportedLanguages;
import com.dgladyshev.deadcodedetector.services.InspectionService;
import com.dgladyshev.deadcodedetector.services.UrlCheckerService;
import com.dgladyshev.deadcodedetector.util.GitHubRepositoryName;
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
    private final UrlCheckerService urlCheckerService;

    @Autowired
    public DeadCodeController(InspectionService inspectionService, UrlCheckerService urlCheckerService) {
        this.inspectionService = inspectionService;
        this.urlCheckerService = urlCheckerService;
    }

    @RequestMapping(value = "/inspections", method = RequestMethod.POST)
    @ResponseBody
    public Inspection addInspection(@RequestParam String url, @RequestParam SupportedLanguages language) {
        String trimmedUrl = trimToEmpty(url);
        log.info("Incoming request for analysis, url: {}, language: {}", trimmedUrl, language);
        GitRepo gitRepo = toGitRepo(trimmedUrl, language.getName());
        urlCheckerService.checkAccessibility(trimmedUrl.replace(".git", ""));
        Inspection inspection = inspectionService.createInspection(gitRepo);
        inspectionService.inspectCode(inspection.getInspectionId());
        return inspection;
    }

    @RequestMapping(value = "/inspections", method = RequestMethod.GET)
    @ResponseBody
    public Collection<Inspection> getInspections() {
        return inspectionService.getInspections().values();
    }

    @RequestMapping(value = "/inspections/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Inspection getInspectionById(@PathVariable String id) {
        return inspectionService.getInspection(id);
    }

    @RequestMapping(value = "/inspections/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteInspectionById(@PathVariable String id) {
        inspectionService.deleteInspection(id);
    }

    private GitRepo toGitRepo(@RequestParam String url, @RequestParam String language) {
        GitHubRepositoryName parsedUrl = GitHubRepositoryName.create(url);
        return GitRepo.builder()
                .host(parsedUrl.getHost())
                .name(parsedUrl.getRepositoryName())
                .user(parsedUrl.getUserName())
                .url(url)
                .language(language)
                .build();
    }

}

