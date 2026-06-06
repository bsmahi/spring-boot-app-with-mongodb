package com.springapp.mongodb.repository;

import com.springapp.mongodb.config.MongoDBTestContainerConfig;
import com.springapp.mongodb.model.Course;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CourseRepositoryTest extends MongoDBTestContainerConfig {

    @Autowired
    private CourseRepository repository;

    @Test
    void testSaveAndFind() {
        repository.deleteAll();

        Course course = new Course("Spring Boot 4", "Test Desc", true);
        Course saved = repository.save(course);

        Optional<Course> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Spring Boot 4");
    }
}