package com.springapp.mongodb.repository;

import com.springapp.mongodb.model.Course;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<@NonNull Course, @NonNull String> {

    List<Course> findByTitleContaining(String title);

}
