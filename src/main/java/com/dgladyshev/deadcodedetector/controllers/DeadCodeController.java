package com.dgladyshev.deadcodedetector.controllers;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.SupportedLanguages;
import com.dgladyshev.deadcodedetector.exceptions.MalformedRequestException;
import com.dgladyshev.deadcodedetector.services.CodeAnalyzerService;
import com.dgladyshev.deadcodedetector.services.InspectionsService;
import com.dgladyshev.deadcodedetector.services.UrlCheckerService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
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

    @PostMapping(value = "/inspections")
    public Mono<Inspection> addInspection(@RequestParam String url,
                                          @RequestParam SupportedLanguages language,
                                          @RequestParam(defaultValue = "master") String branch) {
        GitRepo gitRepo = new GitRepo(url);
        urlCheckerService.checkAccessibility(
                trimToEmpty(url).replace(".git", "")
        );
        return inspectionsService
                .createInspection(
                        gitRepo,
                        language.getName(), trimToEmpty(branch),
                        trimToEmpty(url)
                )
                .doOnSuccess(codeAnalyzerService::inspectCode);
    }

    @PostMapping(value = "/inspections/refresh")
    public Mono<ResponseEntity<Void>> refreshInspection(@RequestParam String url,
                                                        @RequestParam(defaultValue = "master") String branch) {
        GitRepo gitRepo = new GitRepo(url);
        return inspectionsService
                .getRefreshableInspection(
                        gitRepo,
                        trimToEmpty(branch) //TODO move to constructor if possible
                )
                .doOnSuccess(codeAnalyzerService::inspectCode)
                //TODO throw exception which will result in code 404 instead
                .then()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(404).body(null));
    }

    @GetMapping(value = "/inspections")
    public List<Inspection> getInspections(@RequestParam(required = false) Long pageNumber,
                                           @RequestParam(required = false) Long pageSize) {
        if (pageNumber != null && pageSize != null) {
            if (pageNumber < 1 || pageSize < 0) {
                throw new MalformedRequestException("Page number must equal or bigger than 1,"
                        + " page size must be bigger than 0");
            } else {
                //TODO add real pagination after it will be supported by reactive repository
                //return inspectionsService.getPaginatedInspections(pageNumber - 1, pageSize);
                return inspectionsService
                        .getInspections()
                        .skip((pageNumber - 1) * pageSize)
                        .toStream()
                        .limit(pageSize)
                        .collect(Collectors.toList());
            }
        } else {
            return inspectionsService.getInspections().toStream().collect(Collectors.toList());
        }
    }

    @GetMapping(value = "/inspections/{id}")
    public Mono<ResponseEntity<Inspection>> getInspectionById(
            @PathVariable String id,
            @RequestParam(defaultValue = "", required = false) String filter) {
        return inspectionsService.getInspection(id)
                .map(inspection -> inspection.toFilteredInspection(filter))
                //TODO throw exception which will result in code 404 instead
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(404).body(null));
    }

    @DeleteMapping(value = "/inspections/{id}")
    public Mono<Void> deleteInspectionById(@PathVariable String id) { //TODO add NotNull somehow
        return inspectionsService.deleteInspection(id);
    }

}

