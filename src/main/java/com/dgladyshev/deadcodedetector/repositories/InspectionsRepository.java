package com.dgladyshev.deadcodedetector.repositories;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.InspectionState;
import java.util.List;
import java.util.Set;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SuppressWarnings("PMD.MethodNamingConventions")
// must contain _ for Spring Data to generate query
public interface InspectionsRepository extends ReactiveCrudRepository<Inspection, String> {

    Flux<Inspection> findByGitRepo(Mono<GitRepo> repo);

    Flux<Inspection> findByGitRepoAndBranch(Mono<GitRepo> repo, Mono<String> branch);

    List<Inspection> findByGitRepo_Name_AndGitRepo_User_AndGitRepo_Host(String name, String user, String
            host);

    Set<Inspection> findAllByStateNotContains(InspectionState state);

    Set<Inspection> findAllByStateContains(InspectionState state);

}