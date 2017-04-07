package com.dgladyshev.deadcodedetector.repositories;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SuppressWarnings("PMD.MethodNamingConventions")
// must contain _ for Spring Data to generate query
public interface InspectionsRepository extends ReactiveCrudRepository<Inspection, String> {

    Flux<Inspection> findByGitRepo(Mono<GitRepo> repo);

    Flux<Inspection> findByGitRepoAndBranch(Mono<GitRepo> repo, Mono<String> branch);

    Flux<Inspection> findByGitRepo_Name_AndGitRepo_User_AndGitRepo_Host(String name, String user, String
            host);

//    Flux<Inspection> findAllByStateNotContains(InspectionState state);
//
//    Flux<Inspection> findAllByStateContains(InspectionState state);

}