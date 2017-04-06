package com.dgladyshev.deadcodedetector.controllers;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.entities.Branch;
import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.SupportedLanguages;
import com.dgladyshev.deadcodedetector.services.CodeAnalyzerService;
import com.dgladyshev.deadcodedetector.services.InspectionsService;
import com.dgladyshev.deadcodedetector.services.UrlCheckerService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/")
public class DeadCodeController {

    private final CodeAnalyzerService codeAnalyzerService;
    private final InspectionsService inspectionsService;
    private final UrlCheckerService urlCheckerService;

    @Autowired
    public DeadCodeController(CodeAnalyzerService codeAnalyzerService,
                              InspectionsService inspectionsService,
                              UrlCheckerService urlCheckerService) {
        this.codeAnalyzerService = codeAnalyzerService;
        this.inspectionsService = inspectionsService;
        this.urlCheckerService = urlCheckerService;
    }

    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "repositoryUrl",
                    value = "Repository url",
                    required = true,
                    dataType = "string",
                    paramType = "query"),
            @ApiImplicitParam(
                    name = "branch",
                    value = "Branch name",
                    required = false,
                    dataType = "string",
                    paramType = "query"),
            @ApiImplicitParam(
                    name = "language",
                    value = "Language name",
                    required = true,
                    dataType = "string",
                    paramType = "query")
    })
    @PostMapping(value = "/inspections")
    public Inspection addInspection(
            @ApiIgnore @Valid @ModelAttribute(name = "repositoryUrl") GitRepo repo,
            @ApiIgnore @ModelAttribute SupportedLanguages language,
            @ApiIgnore @Valid @ModelAttribute(name = "branch") Branch branch) {
        //TODO consider creating an interface
        urlCheckerService.checkAccessibility(repo.getUrl());
        Inspection inspection = inspectionsService.createInspection(
                repo,
                language.name(),
                branch.getName()
        );
        codeAnalyzerService.inspectCode(inspection);
        return inspection;
    }

    @ModelAttribute
    public SupportedLanguages supportedLanguages(@RequestParam("language") String language) {
        return SupportedLanguages.fromName(language);
    }

    @PostMapping(value = "/inspections/refresh")
    public void refreshInspection(@RequestParam String url,
                                  @RequestParam(defaultValue = "master") String branch) {
        log.info("Incoming request for refreshing an inspection, url: {}, branch: {}", url, branch);
        GitRepo gitRepo = new GitRepo(url);
        Inspection inspection = inspectionsService.getRefreshableInspection(gitRepo, trimToEmpty(branch));
        codeAnalyzerService.inspectCode(inspection);
    }

    @GetMapping(value = "/inspections")
    public Collection<Inspection> getInspections(@Min(value = 1) @RequestParam(required = false) Integer pageNumber,
                                                 @Min(value = 0) @RequestParam(required = false) Integer pageSize) {
        return pageNumber != null && pageSize != null
                ? inspectionsService.getPaginatedInspections(pageNumber - 1, pageSize)
                : inspectionsService.getInspections();
    }

    @GetMapping(value = "/inspections/{id}")
    public Inspection getInspectionById(@PathVariable Long id,
                                        @RequestParam(required = false) String filter) {
        Inspection inspection = inspectionsService.getInspection(id);
        return (StringUtils.isEmptyOrNull(filter))
                ? inspection
                : inspection.toFilteredInspection(filter);
    }

    @DeleteMapping(value = "/inspections/{id}")
    public void deleteInspectionById(@PathVariable Long id) {
        inspectionsService.deleteInspection(id);
    }

}

