package com.dgladyshev.deadcodedetector.controllers;

import com.dgladyshev.deadcodedetector.entities.Branch;
import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.Language;
import com.dgladyshev.deadcodedetector.services.CodeAnalyzerService;
import com.dgladyshev.deadcodedetector.services.InspectionsService;
import com.dgladyshev.deadcodedetector.services.UrlCheckerService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Mono<Inspection> addInspection(
            @ApiIgnore @Valid @ModelAttribute("repositoryUrl") GitRepo repo,
            @ApiIgnore @Valid @ModelAttribute("language") Language language,
            @ApiIgnore @Valid @ModelAttribute("branch") Branch branch) {
        urlCheckerService.checkAccessibility(repo.getUrl());
        return inspectionsService
                .createInspection(
                        repo,
                        language.getName(),
                        branch.getName()
                )
                .doOnSuccess(codeAnalyzerService::inspectCode);
    }

    @PostMapping(value = "/inspections/refresh")
    public Mono<ResponseEntity<Void>> refreshInspection(
            @ApiIgnore @Valid @ModelAttribute("repositoryUrl") GitRepo gitRepo,
            @ApiIgnore @Valid @ModelAttribute("branch") Branch branch) {
        return inspectionsService
                .getRefreshableInspection(
                        gitRepo,
                        branch.getName()
                )
                .doOnSuccess(codeAnalyzerService::inspectCode)
                //TODO throw exception which will result in code 404 instead
                .then()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(404).body(null));
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
                //TODO throw exception which will result in code 404 instead
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(404).body(null));
    }

    @DeleteMapping(value = "/inspections/{id}")
    public Mono<Void> deleteInspectionById(@PathVariable String id) {
        return inspectionsService.deleteInspection(id);
    }

}