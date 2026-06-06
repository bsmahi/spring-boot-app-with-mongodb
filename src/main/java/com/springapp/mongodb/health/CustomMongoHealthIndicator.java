package com.springapp.mongodb.health;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

@Component
public class CustomMongoHealthIndicator extends AbstractHealthIndicator {

    @Value("${spring.mongodb.database}")
    private String databaseName;

    private final MongoClient mongoClient;

    public CustomMongoHealthIndicator(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            // Ping your actual database (course-db)
            mongoClient.getDatabase(databaseName)
                    .runCommand(new Document("ping", 1));
            builder.up()
                    .withDetail("MongoDB", "Connected")
                    .withDetail("Database", databaseName);
        } catch (Exception e) {
            builder.down()
                    .withDetail("MongoDB", "Connection failed")
                    .withDetail("Error", e.getMessage());
        }
    }
}
