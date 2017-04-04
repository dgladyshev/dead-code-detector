package com.dgladyshev.deadcodedetector.repositories;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InspectionsRepository extends ReactiveCrudRepository<Inspection, String> {

    Flux<Inspection> findByGitRepo(Mono<GitRepo> repo);

    Flux<Inspection> findByGitRepoAndBranch(Mono<GitRepo> repo, Mono<String> branch);
}