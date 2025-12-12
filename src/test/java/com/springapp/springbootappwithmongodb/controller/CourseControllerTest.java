package com.springapp.springbootappwithmongodb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springapp.springbootappwithmongodb.config.MongoDBTestContainerConfig;
import com.springapp.springbootappwithmongodb.model.Course;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CourseControllerTest extends MongoDBTestContainerConfig {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testCreateAndFetchCourse() throws Exception {
        // 1. Create course
        Course course = new Course("Spring Boot 4", "Test Desc", true);
        String json = objectMapper.writeValueAsString(course);

        // Create a course
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        // Now DB is clean -> Only this 1 course exists
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring Boot 4"));
    }

    @Test
    void testGetCourseById() throws Exception {
        // Create
        Course course = new Course("Docker", "Containers", true);
        String json = objectMapper.writeValueAsString(course);

        String location = mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        String id = location.substring(location.lastIndexOf('/') + 1);

        // Get by ID
        mockMvc.perform(get("/api/courses/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Docker"))
                .andExpect(jsonPath("$.description").value("Containers"));
    }

    @Test
    void testGetCourseById_1() throws Exception {
        // Create
        Course course = new Course("Spring Boot 4", "Test Containers", true);
        String json = objectMapper.writeValueAsString(course);

        String location = mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        String id = location.substring(location.lastIndexOf('/') + 1);

        // Get by ID
        mockMvc.perform(get("/api/courses/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Boot 4"))
                .andExpect(jsonPath("$.description").value("Test Containers"));
    }
}
