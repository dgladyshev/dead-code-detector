package com.dgladyshev.deadcodedetector.controllers;

import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_ID;
import static com.dgladyshev.deadcodedetector.controllers.constants.ControllerExpectedEntities.EXPECTED_INSPECTION;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.services.CodeAnalyzerService;
import com.dgladyshev.deadcodedetector.services.InspectionsService;
import com.dgladyshev.deadcodedetector.services.UrlCheckerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SuppressWarnings("PMD.CommentSize")
public class DeadCodeControllerTest {

    private static final String API_V1_INSPECTIONS = "/api/v1/inspections";

    private final InspectionsService inspectionsService = mock(InspectionsService.class);

    private final WebTestClient client = WebTestClient
            .bindToController(
                    new DeadCodeController(
                            mock(CodeAnalyzerService.class),
                            inspectionsService,
                            mock(UrlCheckerService.class)
                    )
            )
            .build();

    @Test
    public void testGetInspections() throws Exception {
        given(inspectionsService.getInspections()).willReturn(
                Flux.just(EXPECTED_INSPECTION, EXPECTED_INSPECTION, EXPECTED_INSPECTION)
        );

        FluxExchangeResult<Inspection> result = client.get().uri(API_V1_INSPECTIONS)
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(TEXT_EVENT_STREAM)
                .expectBody(Inspection.class)
                .returnResult();

        StepVerifier.create(result.getResponseBody())
                .expectNext(EXPECTED_INSPECTION)
                .expectNext(EXPECTED_INSPECTION)
                .expectNextCount(1)
                .thenCancel()
                .verify();
    }

    @Test
    public void testGetInspectionById() throws Exception {
        given(inspectionsService.getInspection(EXPECTED_ID)).willReturn(Mono.just(EXPECTED_INSPECTION));

        FluxExchangeResult<Inspection> result = client.get().uri(API_V1_INSPECTIONS + "/" + EXPECTED_ID)
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(TEXT_EVENT_STREAM)
                .expectBody(Inspection.class)
                .returnResult();

        StepVerifier.create(result.getResponseBody())
                .expectNext(EXPECTED_INSPECTION)
                .thenCancel()
                .verify();
    }

    @Test
    public void testDeleteInspectionById() throws Exception {
        given(inspectionsService.getInspection(EXPECTED_ID)).willReturn(Mono.just(EXPECTED_INSPECTION));
        given(inspectionsService.deleteInspection(EXPECTED_INSPECTION)).willReturn(Mono.empty());

        FluxExchangeResult<Void> result = client.delete().uri(API_V1_INSPECTIONS + "/" + EXPECTED_ID)
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class)
                .returnResult();

        StepVerifier.create(result.getResponseBody())
                .expectNextCount(0)
                .thenCancel()
                .verify();
    }

    /*    @Test //TODO fix
        public void testAddInspectionById() throws Exception {
            given(inspectionsService.createInspection(
                    EXPECTED_REPO,
                    EXPECTED_LANGUAGE,
                    EXPECTED_BRANCH
            )).willReturn(Mono.just(EXPECTED_INSPECTION));

            FluxExchangeResult<Inspection> result = client.post()
                    .uri(
                            new URIBuilder()
                                    .setPath(API_V1_INSPECTIONS)
                                    .setParameter("repositoryUrl", EXPECTED_REPO_URL)
                                    .setParameter("branch", EXPECTED_BRANCH)
                                    .setParameter("language", EXPECTED_LANGUAGE)
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
                    .thenCancel()
                    .verify();
        }*/

}