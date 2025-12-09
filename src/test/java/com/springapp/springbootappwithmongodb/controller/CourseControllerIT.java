package com.springapp.springbootappwithmongodb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springapp.springbootappwithmongodb.model.Course;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CourseControllerIT {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Container
    static LgtmStackContainer lgtm = new LgtmStackContainer("grafana/otel-lgtm:0.11.1")
            .withReuse(true)
            .withExposedPorts(3000, 4318, 4317);


    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        String mongoUri = mongo.getReplicaSetUrl();
        System.out.println("=== TESTCONTAINERS MongoDB URI: " + mongoUri + " ===");

        // Use a different property name and map it
        registry.add("mongodb.uri", () -> mongoUri);
        registry.add("spring.mongodb.uri", () -> mongoUri);

        // Also try individual properties
        registry.add("spring.mongodb.host", mongo::getHost);
        registry.add("spring.mongodb.port", () -> mongo.getMappedPort(27017));
        registry.add("spring.mongodb.database", () -> "test");
        registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("management.otlp.metrics.export.url",
                () -> "http://" + lgtm.getHost() + ":" + lgtm.getMappedPort(4318) + "/v1/metrics");

        registry.add("otel.exporter.otlp.endpoint",
                () -> "http://" + lgtm.getHost() + ":" + lgtm.getMappedPort(4318));

        // disable OTLP export completely in tests
        registry.add("management.otlp.metrics.export.enabled", () -> false);
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
