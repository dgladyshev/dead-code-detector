package com.dgladyshev.deadcodedetector.controllers;

import static org.mockito.Mockito.mock;

import com.dgladyshev.deadcodedetector.services.InspectionsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SuppressWarnings({"PMD.CommentSize", "PMD.UnusedPrivateField"})
public class GitRepositoriesControllerTest {

    private static final String API_V1_REPOSITORIES = "/api/v1/repositories/";

    private final InspectionsService inspectionsService = mock(InspectionsService.class);

    private final WebTestClient client = WebTestClient
            .bindToController(
                    new GitRepositoriesController(
                            inspectionsService
                    )
            )
            .build();

    @Test //TODO fix
    public void testGetInspections() throws Exception {
    //        given(inspectionsService.getRepositoryInspections(EXPECTED_REPO)).willReturn(
    //                Flux.just(EXPECTED_INSPECTION, EXPECTED_INSPECTION)
    //        );
    //
    //        FluxExchangeResult<Inspection> result = client.get()
    //                .uri(
    //                        new URIBuilder()
    //                                .setPath(API_V1_REPOSITORIES + "inspections")
    //                                .setParameter("repositoryUrl", EXPECTED_REPO_URL)
    //                                .build()
    //                )
    //                .accept(TEXT_EVENT_STREAM)
    //                .exchange()
    //                .expectStatus().isOk()
    //                .expectHeader().contentType(TEXT_EVENT_STREAM)
    //                .expectBody(Inspection.class)
    //                .returnResult();
    //
    //        StepVerifier.create(result.getResponseBody())
    //                .expectNext(EXPECTED_INSPECTION)
    //                .expectNext(EXPECTED_INSPECTION)
    //                .expectNextCount(0)
    //                .thenCancel()
    //                .verify();
    }

}