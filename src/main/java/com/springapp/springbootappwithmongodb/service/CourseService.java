package com.springapp.springbootappwithmongodb.service;

import com.springapp.springbootappwithmongodb.model.Course;

import java.util.List;
import java.util.Optional;

public interface CourseService {

    Optional<List<Course>> findAll();

    Optional<List<Course>> findByTitleContaining(String title);

    Optional<Course> findById(String id);

    Optional<Course> createCourse(Course course);

    void deleteAllCourses();

    void deleteCourseById(String id);

}
