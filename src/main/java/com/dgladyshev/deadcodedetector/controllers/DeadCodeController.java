package com.dgladyshev.deadcodedetector.controllers;

import com.dgladyshev.deadcodedetector.entities.Branch;
import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.Language;
import com.dgladyshev.deadcodedetector.exceptions.InspectionIsLockedException;
import com.dgladyshev.deadcodedetector.services.CodeAnalyzerService;
import com.dgladyshev.deadcodedetector.services.InspectionsService;
import com.dgladyshev.deadcodedetector.services.UrlCheckerService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import springfox.documentation.annotations.ApiIgnore;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/")
public class DeadCodeController {

    private static final String REPOSITORY_URL = "repositoryUrl";
    private static final String BRANCH = "branch";
    private static final String LANGUAGE = "language";
    private static final String STRING_DATA_TYPE = "string";
    private static final String QUERY_PARAM_TYPE = "query";

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
                    name = REPOSITORY_URL,
                    value = "Repository url",
                    required = true,
                    dataType = STRING_DATA_TYPE,
                    paramType = QUERY_PARAM_TYPE),
            @ApiImplicitParam(
                    name = BRANCH,
                    value = "Branch name",
                    required = false,
                    dataType = STRING_DATA_TYPE,
                    paramType = QUERY_PARAM_TYPE),
            @ApiImplicitParam(
                    name = LANGUAGE,
                    value = "Language name",
                    required = true,
                    dataType = STRING_DATA_TYPE,
                    paramType = QUERY_PARAM_TYPE)
    })
    @PostMapping(value = "/inspections")
    public Mono<Inspection> addInspection(
            @ApiIgnore @Valid @ModelAttribute(REPOSITORY_URL) GitRepo repo,
            @ApiIgnore @Valid @ModelAttribute(LANGUAGE) Language language,
            @ApiIgnore @Valid @ModelAttribute(BRANCH) Branch branch) {
        urlCheckerService.checkAccessibility(repo.getUrl());
        return inspectionsService
                .createInspection(
                        repo,
                        language.getName(),
                        branch.getName()
                )
                .doOnSuccess(codeAnalyzerService::inspectCode);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = REPOSITORY_URL,
                    value = "Repository url",
                    required = true,
                    dataType = STRING_DATA_TYPE,
                    paramType = QUERY_PARAM_TYPE),
            @ApiImplicitParam(
                    name = BRANCH,
                    value = "Branch name",
                    required = true,
                    dataType = STRING_DATA_TYPE,
                    paramType = QUERY_PARAM_TYPE)
    })
    @PostMapping(value = "/inspections/refresh")
    public Mono<ResponseEntity<Void>> refreshInspection(
            @ApiIgnore @Valid @ModelAttribute(REPOSITORY_URL) GitRepo gitRepo,
            @ApiIgnore @Valid @ModelAttribute(BRANCH) Branch branch) {
        return inspectionsService
                .getInspection(
                        gitRepo,
                        branch.getName()
                )
                .filter(inspection -> {
                    if (!inspection.isFinished()) {
                        throw new InspectionIsLockedException();
                    }
                    return true;
                })
                .doOnSuccess(codeAnalyzerService::inspectCode)
                .map(inspection -> new ResponseEntity<Void>(HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(value = "/inspections")
    public Flux<Inspection> getInspections(@Min(value = 1) @RequestParam(required = false) Long pageNumber,
                                           @Min(value = 0) @RequestParam(required = false) Long pageSize) {
        return pageNumber != null && pageSize != null
                ? Flux.fromStream(
                inspectionsService
                        .getInspections()
                        .skip((pageNumber - 1) * pageSize)
                        .toStream()
                        .limit(pageSize))
                : inspectionsService.getInspections();
    }

    @GetMapping(value = "/inspections/{id}")
    public Mono<ResponseEntity<Inspection>> getInspectionById(
            @PathVariable String id,
            @RequestParam(required = false) String filter) {
        return inspectionsService.getInspection(id)
                .map(inspection -> inspection.toFilteredInspection(filter))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(404).body(null));
    }

    @DeleteMapping(value = "/inspections/{id}")
    public Mono<ResponseEntity<Void>> deleteInspectionById(@PathVariable String id) {
        return inspectionsService
                .getInspection(id)
                .filter(inspection -> {
                    if (!inspection.isFinished()) {
                        throw new InspectionIsLockedException();
                    }
                    return true;
                })
                .map(inspectionsService::deleteInspection)
                .map(Void -> new ResponseEntity<Void>(HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

}