package com.springapp.springbootappwithmongodb.repository;

import com.springapp.springbootappwithmongodb.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    List<Course> findByTitleContaining(String title);

}
