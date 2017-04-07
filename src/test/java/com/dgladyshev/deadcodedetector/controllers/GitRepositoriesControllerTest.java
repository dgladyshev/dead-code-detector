package com.dgladyshev.deadcodedetector.controllers;

import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_INSPECTION;
import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_REPO;
import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_REPO_URL;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.services.InspectionsService;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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

    @Test
    public void testGetInspections() throws Exception {
        given(inspectionsService.getRepositoryInspections(EXPECTED_REPO)).willReturn(
                Flux.just(EXPECTED_INSPECTION, EXPECTED_INSPECTION)
        );

        FluxExchangeResult<Inspection> result = client.get()
                .uri(
                        new URIBuilder()
                                .setPath(API_V1_REPOSITORIES + "inspections")
                                .setParameter("repositoryUrl", EXPECTED_REPO_URL)
                                .build()
                )
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(TEXT_EVENT_STREAM)
                .expectBody(Inspection.class)
                .returnResult();

        StepVerifier.create(result.getResponseBody())
                .expectNext(EXPECTED_INSPECTION)
                .expectNext(EXPECTED_INSPECTION)
                .expectNextCount(0)
                .thenCancel()
                .verify();
    }

}