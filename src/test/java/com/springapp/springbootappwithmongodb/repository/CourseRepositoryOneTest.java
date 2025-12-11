package com.springapp.springbootappwithmongodb.repository;

import com.springapp.springbootappwithmongodb.model.Course;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
public class CourseRepositoryOneTest {
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

    @Autowired
    private CourseRepository repository;

    @Test
    void testSaveAndFind_1() {
        repository.deleteAll();

        Course course = new Course("Spring Boot 4.1", "Test Desc 1", true);
        Course saved = repository.save(course);

        Optional<Course> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Spring Boot 4.1");
    }
}
