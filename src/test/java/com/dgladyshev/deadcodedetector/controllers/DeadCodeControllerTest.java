package com.dgladyshev.deadcodedetector.controllers;

import com.dgladyshev.deadcodedetector.services.InspectionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(profiles = {"local"})
@RunWith(SpringRunner.class)
@WebMvcTest(DeadCodeController.class)
public class DeadCodeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private InspectionService inspectionService;

	@Test
	public void testGetInspections() throws Exception {
		this.mockMvc.perform(get("/api/v1/inspections")
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk());
	}

	@Test
	public void testGetInspectionById() throws Exception {
		this.mockMvc.perform(get("/api/v1/inspections/someId")
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk());
	}

	@Test
	public void testDeleteInspectionById() throws Exception {
		this.mockMvc.perform(get("/api/v1/inspections/someId")
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk());
	}

	@Test
	public void testAddInspectionById() throws Exception {
		this.mockMvc.perform(get("/api/v1/inspections?url=https://github.com/dgladyshev/dead-code-detector.git&language=JAVA")
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk());
	}


}