package com.dgladyshev.deadcodedetector.controllers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.repositories.GitRepositoriesRepository;
import com.dgladyshev.deadcodedetector.services.InspectionsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.unitils.reflectionassert.ReflectionAssert;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SuppressWarnings("PMD.UnusedPrivateField")
public class GitRepositoriesControllerTest {

    private static final Inspection EXPECTED_INSPECTION = Inspection
            .builder()
            .inspectionId("someId")
            .build();

    private static final String REPOSITORY_URL = "https://github.com/user/repo";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InspectionsService inspectionsService;

    @MockBean
    private GitRepositoriesRepository gitRepositoriesRepository;

    @Test
    public void testGetInspectionsIds() throws Exception {
        HashSet<String> expectedResult = Sets.newHashSet(EXPECTED_INSPECTION.getId());
        given(gitRepositoriesRepository.getRepositoryInspections(any())).willReturn(expectedResult);
        ResultActions result = this.mockMvc
                .perform(get("/api/v1/repositories/inspections_ids")
                                 .param("url", REPOSITORY_URL)
                                 .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk());
        String jsonString = result.andReturn().getResponse().getContentAsString();
        Set<String> actualResult = mapper.readValue(jsonString, new TypeReference<Set<String>>() {
        });
        ReflectionAssert.assertReflectionEquals(
                expectedResult,
                actualResult
        );
    }


    @Test
    public void testGetInspections() throws Exception {
        String expectedInspectionId = EXPECTED_INSPECTION.getId();
        HashSet<String> expectedInspectionsIds = Sets.newHashSet(expectedInspectionId);
        List<Inspection> expectedResult = Lists.newArrayList(EXPECTED_INSPECTION);
        given(gitRepositoriesRepository.getRepositoryInspections(any())).willReturn(expectedInspectionsIds);
        given(inspectionsService.getInspection(expectedInspectionId)).willReturn(EXPECTED_INSPECTION);
        ResultActions result = this.mockMvc
                .perform(get("/api/v1/repositories/inspections")
                                 .param("url", REPOSITORY_URL)
                                 .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk());
        String jsonString = result.andReturn().getResponse().getContentAsString();
        List<Inspection> actualResult = mapper.readValue(jsonString, new TypeReference<List<Inspection>>() {
        });
        ReflectionAssert.assertReflectionEquals(
                expectedResult,
                actualResult
        );
    }

}