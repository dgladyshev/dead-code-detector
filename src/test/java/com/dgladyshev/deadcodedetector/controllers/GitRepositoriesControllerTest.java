package com.dgladyshev.deadcodedetector.controllers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.services.InspectionsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.List;
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
            .id(123L)
            .build();

    private static final String REPOSITORY_URL = "https://github.com/user/repo";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InspectionsService inspectionsService;

    @Test
    public void testGetInspections() throws Exception {
        List<Inspection> expectedResult = Lists.newArrayList(EXPECTED_INSPECTION);
        given(inspectionsService.getRepositoryInspections(any())).willReturn(expectedResult);
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