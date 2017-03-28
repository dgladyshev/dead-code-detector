package com.dgladyshev.deadcodedetector.controllers;

import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.repositories.GitRepositoriesRepository;
import com.dgladyshev.deadcodedetector.repositories.InspectionsRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/")
public class GitRepositoriesController {

    private final GitRepositoriesRepository gitRepositoriesRepository;
    private final InspectionsRepository inspectionsRepository;

    @Autowired
    public GitRepositoriesController(GitRepositoriesRepository gitRepositoriesRepository,
                                     InspectionsRepository inspectionsRepository) {
        this.gitRepositoriesRepository = gitRepositoriesRepository;
        this.inspectionsRepository = inspectionsRepository;
    }

    @RequestMapping(
            value = "/repositories/inspections_ids",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Set<String> getRepositoryInspectionsIds(@RequestParam String url) {
        GitRepo gitRepo = new GitRepo(url);
        return gitRepositoriesRepository.getRepositoryInspections(gitRepo);
    }

    @RequestMapping(
            value = "/repositories/inspections",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Inspection> getRepositoryInspections(@RequestParam String url) {
        GitRepo gitRepo = new GitRepo(url);
        return gitRepositoriesRepository.getRepositoryInspections(gitRepo).stream()
                .map(id -> inspectionsRepository.getInspection(id))
                .collect(Collectors.toList());
    }


}

