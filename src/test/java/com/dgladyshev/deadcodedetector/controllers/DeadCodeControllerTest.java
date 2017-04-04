package com.dgladyshev.deadcodedetector.controllers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.services.CodeAnalyzerService;
import com.dgladyshev.deadcodedetector.services.InspectionsService;
import com.dgladyshev.deadcodedetector.services.UrlCheckerService;
import org.apache.http.client.utils.URIBuilder;
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
@SuppressWarnings("PMD.UnusedPrivateField")
public class DeadCodeControllerTest {

    private static final String EXPECTED_ID = "some-unique-id";
    private static final String EXPECTED_REPO_URL = "https://github.com/dgladyshev/dead-code-detector.git";
    private static final String EXPECTED_LANGUAGE = "JAVA";
    private static final String EXPECTED_BRANCH = "master";

    private static final Inspection EXPECTED_INSPECTION = Inspection
            .builder()
            .id(EXPECTED_ID)
            .url(EXPECTED_REPO_URL)
            .language(EXPECTED_LANGUAGE)
            .branch(EXPECTED_BRANCH)
            .build();

    private InspectionsService inspectionsService = mock(InspectionsService.class);

    private WebTestClient client = WebTestClient
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
        //TODO test return of multiple items
        given(inspectionsService.getInspections()).willReturn(Flux.just(EXPECTED_INSPECTION));

        FluxExchangeResult<Inspection> result = client.get().uri("/api/v1/inspections")
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
    public void testGetInspectionById() throws Exception {
        given(inspectionsService.getInspection(EXPECTED_ID)).willReturn(Mono.just(EXPECTED_INSPECTION));

        FluxExchangeResult<Inspection> result = client.get().uri("/api/v1/inspections/" + EXPECTED_ID)
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
        given(inspectionsService.deleteInspection(EXPECTED_ID)).willReturn(Mono.empty());

        FluxExchangeResult<Inspection> result = client.delete().uri("/api/v1/inspections/" + EXPECTED_ID)
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Inspection.class)
                .returnResult();

        StepVerifier.create(result.getResponseBody())
                .expectNextCount(0)
                .thenCancel()
                .verify();
    }

    @Test
    public void testAddInspectionById() throws Exception {
        GitRepo gitRepo = new GitRepo(EXPECTED_REPO_URL);
        given(inspectionsService.createInspection(
                gitRepo,
                EXPECTED_LANGUAGE.toLowerCase(),
                EXPECTED_BRANCH,
                EXPECTED_REPO_URL
        )).willReturn(Mono.just(EXPECTED_INSPECTION));
        FluxExchangeResult<Inspection> result = client.post()
                .uri(
                        new URIBuilder()
                                .setPath("/api/v1/inspections/")
                                .setParameter("url", EXPECTED_REPO_URL)
                                .setParameter("language", EXPECTED_LANGUAGE)
                                .setParameter("branch", EXPECTED_BRANCH)
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
    }

}