package com.dgladyshev.deadcodedetector.controllers;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.services.InspectionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/v1/")
public class GitRepositoriesController {

    private final InspectionsService inspectionsService;

    @Autowired
    public GitRepositoriesController(InspectionsService inspectionsService) {
        this.inspectionsService = inspectionsService;
    }

    @GetMapping(value = "/repositories/inspections")
    public Flux<Inspection> getRepositoryInspections(@RequestParam String url) {
        GitRepo gitRepo = new GitRepo(url);
        return inspectionsService.getRepositoryInspections(gitRepo);
    }

}

