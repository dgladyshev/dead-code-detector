package com.dgladyshev.deadcodedetector.controllers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.repositories.InspectionsRepository;
import com.dgladyshev.deadcodedetector.services.InspectionService;
import com.dgladyshev.deadcodedetector.services.UrlCheckerService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.HashMap;
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
import org.springframework.web.client.RestTemplate;
import org.unitils.reflectionassert.ReflectionAssert;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SuppressWarnings("PMD.UnusedPrivateField")
public class DeadCodeControllerTest {

    private static final Inspection EXPECTED_INSPECTION = Inspection
            .builder()
            .inspectionId("someId")
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InspectionService inspectionService;

    @MockBean
    private InspectionsRepository inspectionsRepository;

    @MockBean
    private UrlCheckerService urlCheckerService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void testGetInspections() throws Exception {
        HashMap<String, Inspection> map = new HashMap<>();
        map.put(EXPECTED_INSPECTION.getInspectionId(), EXPECTED_INSPECTION);
        given(inspectionsRepository.getInspections()).willReturn(map);
        ResultActions result = this.mockMvc
                .perform(get("/api/v1/inspections")
                                 .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk());
        ReflectionAssert.assertReflectionEquals(
                Lists.newArrayList(EXPECTED_INSPECTION),
                toInspections(result)
        );
    }

    @Test
    public void testGetInspectionById() throws Exception {
        String inspectionId = EXPECTED_INSPECTION.getInspectionId();
        given(inspectionsRepository.getInspection(inspectionId)).willReturn(EXPECTED_INSPECTION);
        ResultActions result = this.mockMvc
                .perform(get("/api/v1/inspections/" + inspectionId)
                                 .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk());
        ReflectionAssert.assertReflectionEquals(
                EXPECTED_INSPECTION,
                toInspection(result)
        );
    }

    @Test
    public void testDeleteInspectionById() throws Exception {
        this.mockMvc.perform(delete("/api/v1/inspections/someId")
                                     .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void testAddInspectionById() throws Exception {
        when(inspectionsRepository.createInspection(any(), any() ,any())).thenReturn(EXPECTED_INSPECTION);
        ResultActions result = this.mockMvc.perform(post("/api/v1/inspections")
                                                            .param("url",
                                                                   "https://github.com/dgladyshev/dead-code-detector.git")
                                                            .param("language", "JAVA")
                                                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                                            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk());
        ReflectionAssert.assertReflectionEquals(
                EXPECTED_INSPECTION,
                toInspection(result)
        );
    }

    private Inspection toInspection(ResultActions result) throws java.io.IOException {
        String jsonString = result.andReturn().getResponse().getContentAsString();
        return mapper.readValue(jsonString, Inspection.class);
    }

    private List<Inspection> toInspections(ResultActions result) throws java.io.IOException {
        String jsonString = result.andReturn().getResponse().getContentAsString();
        return mapper.readValue(jsonString, new TypeReference<List<Inspection>>() {
        });
    }

}