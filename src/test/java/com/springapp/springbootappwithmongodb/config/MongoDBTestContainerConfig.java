package com.springapp.springbootappwithmongodb.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class MongoDBTestContainerConfig {

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
}

