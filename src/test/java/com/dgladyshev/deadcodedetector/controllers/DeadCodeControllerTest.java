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
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@SuppressWarnings("PMD.UnusedPrivateField")
public class DeadCodeControllerTest {

    private static final Inspection TEST_INSPECTION = Inspection
            .builder()
            .inspectionId("someId")
            .build();

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
        map.put(TEST_INSPECTION.getInspectionId(), TEST_INSPECTION);
        given(inspectionsRepository.getInspections()).willReturn(map);
        this.mockMvc.perform(get("/api/v1/inspections")
                                     .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk());
        //TODO check returned data as well
    }

    @Test
    public void testGetInspectionById() throws Exception {
        given(inspectionsRepository.getInspection(TEST_INSPECTION.getInspectionId()))
                .willReturn(TEST_INSPECTION);
        this.mockMvc.perform(get("/api/v1/inspections/" + TEST_INSPECTION.getInspectionId())
                                     .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk());
        //TODO check returned data as well
    }

    @Test
    public void testDeleteInspectionById() throws Exception {
        this.mockMvc.perform(delete("/api/v1/inspections/someId")
                                     .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void testAddInspectionById() throws Exception {
        when(inspectionsRepository.createInspection(any())).thenReturn(
                TEST_INSPECTION
        );
        this.mockMvc.perform(post("/api/v1/inspections")
                                     .param("url", "https://github.com/dgladyshev/dead-code-detector.git")
                                     .param("language", "JAVA")
                                     .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                     .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk());
        //TODO check returned data as well
    }


}