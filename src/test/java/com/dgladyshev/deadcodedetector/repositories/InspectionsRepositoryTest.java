package com.dgladyshev.deadcodedetector.repositories;

import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_BRANCH;
import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_ID;
import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_INSPECTION;
import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_REPO;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class InspectionsRepositoryTest {

    @Autowired
    private InspectionsRepository inspectionsRepository;

    @Test
    public void saveInspection() throws Exception {
        inspectionsRepository.save(EXPECTED_INSPECTION).block();
        Mono<Inspection> actualInspection = inspectionsRepository.findOne(EXPECTED_ID);
        StepVerifier.create(actualInspection)
                .expectNext(EXPECTED_INSPECTION)
                .expectNextCount(0)
                .thenCancel()
                .verify();
    }

    @Test
    public void findByGitRepo() throws Exception {
        inspectionsRepository.save(EXPECTED_INSPECTION).block();
        Flux<Inspection> actualInspection = inspectionsRepository.findByGitRepo(
                Mono.just(EXPECTED_REPO)
        );
        StepVerifier.create(actualInspection)
                .expectNext(EXPECTED_INSPECTION)
                .expectNextCount(0)
                .thenCancel()
                .verify();
    }

    @Test
    public void findByGitRepoAndBranch() throws Exception {
        inspectionsRepository.save(EXPECTED_INSPECTION).block();
        Flux<Inspection> actualInspection = inspectionsRepository.findByGitRepoAndBranch(
                Mono.just(EXPECTED_REPO),
                Mono.just(EXPECTED_BRANCH)
        );
        StepVerifier.create(actualInspection)
                .expectNext(EXPECTED_INSPECTION)
                .expectNextCount(0)
                .thenCancel()
                .verify();
    }

    @Test
    public void findByGitRepo_Name_AndGitRepo_User_AndGitRepo_Host() throws Exception {
        inspectionsRepository.save(EXPECTED_INSPECTION).block();
        GitRepo expectedRepo = EXPECTED_REPO;
        Flux<Inspection> actualInspection = inspectionsRepository.findByGitRepo_Name_AndGitRepo_User_AndGitRepo_Host(
                expectedRepo.getName(),
                expectedRepo.getUser(),
                expectedRepo.getHost()
        );
        StepVerifier.create(actualInspection)
                .expectNext(EXPECTED_INSPECTION)
                .expectNextCount(0)
                .thenCancel()
                .verify();
    }

}