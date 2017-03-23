package com.dgladyshev.deadcodedetector.controllers;

import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.services.InspectionService;
import com.dgladyshev.deadcodedetector.util.URLChecker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ActiveProfiles(profiles = {"local"})
@RunWith(PowerMockRunner.class)
@WebMvcTest(DeadCodeController.class)
@WebAppConfiguration
@PrepareForTest({URLChecker.class})
public class DeadCodeControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	private DeadCodeController deadCodeController;

	@Mock
	private InspectionService inspectionService;

	@Before
	public void setup() throws Exception {
		this.mockMvc = standaloneSetup(deadCodeController).build();
		mockStatic(URLChecker.class);
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetInspections() throws Exception {
		this.mockMvc.perform(get("/api/v1/inspections")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(status().isOk());
	}

	@Test
	public void testGetInspectionById() throws Exception {
		this.mockMvc.perform(get("/api/v1/inspections/someId")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(status().isOk());
	}

	@Test
	public void testDeleteInspectionById() throws Exception {
		this.mockMvc.perform(delete("/api/v1/inspections/someId")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(status().isOk());
	}

	@Test
	public void testAddInspectionById() throws Exception {
		//TODO improve
		when(inspectionService.createInspection(any())).thenReturn(
				Inspection.builder().inspectionId("someId").build()
		);
		this.mockMvc.perform(post("/api/v1/inspections")
				.param("url", "https://github.com/dgladyshev/dead-code-detector.git")
				.param("language", "JAVA")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(status().isOk());
	}


}