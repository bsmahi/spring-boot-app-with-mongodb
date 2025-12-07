package com.springapp.springbootappwithmongodb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springapp.springbootappwithmongodb.model.Course;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CourseControllerIT {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.6");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getConnectionString);
    }

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
}
