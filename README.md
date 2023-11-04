# Spring Boot Rest API with MongoDB

We will build a Spring Boot JPA Rest CRUD API for a Course application in that:

- Each Course has id, title, description, published status
- All the Api's help to perform create, retrieve, update, delete Courses
- We have also defined custom query method to retrieve details based on the title and published

## Technologies Used

- Java 17
- Spring Boot 3.1.x
- Spring Modules Covered: Spring Boot Web, Spring Data Jpa, Spring Actuator, OpenAPI
- Database: MongoDB
- Build Tool: Maven

We will develop a Course application by creating a Spring Boot JPA Rest CRUD API.

- Each Course has id, title, description, published status.
- Apis help to create, retrieve, update, delete Tutorials.
- Apis also support custom finder methods such as find by published status or by title.

These are APIs that we need to provide:

| HTTP Method |                    Urls                    |                    Description                    |
|:-----------:|:------------------------------------------:|:-------------------------------------------------:|
|    POST     |                /api/courses                |                 Create New Course                 |
|     GET     |                /api/courses                |                Get All the Courses                |
|     GET     |              /api/courses/:id              |             Retrieve a Course by :id              |
|     PUT     |              /api/courses/:id              |              Update a Course by :id               |
|   DELETE    |              /api/courses/:id              |              Delete a Course by :id               |
|   DELETE    |                /api/courses                |              Delete All the Courses               |
|     GET     | /api/courses/courses-title?title=[keyword] | Retrieve all Courses which title contains keyword |

## Project Folder Structure

![Image](./images/projectstructure.png "Project Structure")

Let me explain it briefly

- `Course.java` data model class corresponds to entity and table courses
- `CourseRepository.java` the interface extends JpaRepository for CRUD methods and custom finder methods. It will be
  autowired in CourseController
- `CourseController.java` the class where we will define all endpoints as a presentation layer
- Configuration for Spring Datasource, JPA & Hibernate in `application.properties`
- `pom.xml` contains all the dependencies required for this application

## Create Spring Boot Project

For pre-initialized project using Spring Initializer, please
click [here](https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.1.5&packaging=jar&jvmVersion=17&groupId=com.springapp&artifactId=spring-boot-app-with-mongodb&name=spring-boot-app-with-mongodb&description=Demo%20project%20for%20Spring%20Boot&packageName=com.springapp.spring-boot-app-with-mongodb&dependencies=web,actuator,data-mongodb)

## Configure Spring Datasource, JPA, Hibernate

```properties
spring.data.mongodb.database=course-db
spring.data.mongodb.port=27017
```

## Define Course Entity

Our Data model is Course with four fields: id, title, description, published.
In model package, we define Course class.

```java
package com.springapp.springbootappwithmongodb.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "courses")
public class Course {

  @Id
  private String id;

  private String title;
  private String description;
  private boolean published;

  public Course() {
  }

  public Course(String title, String description, boolean published) {
    this.title = title;
    this.description = description;
    this.published = published;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isPublished() {
    return published;
  }

  public void setPublished(boolean published) {
    this.published = published;
  }

  @Override
  public String toString() {
    return "Course{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", published=" + published +
            '}';
  }
}
```
- `@Document` annotation helps us override the collection name by “tutorials”.

## CourseRepository

Let's create a repository interface to interact with database operations

In _repository_ folder, create `CourseRepository` interface that `extends MongoRepository`

```java
package com.springapp.springbootappwithmongodb.repository;

import com.springapp.springbootappwithmongodb.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

  List<Course> findByTitleContaining(String title);

}

```

Since Spring is providing boilerplate/implementation code for the `findAll(), findById(), save(), delete()`
and `deleteById()` through `MongoRepository` interface

We can also define the custom methods:

- `findByTitleContaining()`: returns all Courses which title contains input title

## CourseController

Let's create CourseController class define the all the endpoints

```java
package com.springapp.springbootappwithmongodb.controller;

import com.springapp.springbootappwithmongodb.exception.CourseNotFoundException;
import com.springapp.springbootappwithmongodb.model.Course;
import com.springapp.springbootappwithmongodb.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api/courses")
public class CourseController {

  private final CourseService service;

  public CourseController(CourseService service) {
    this.service = service;
  }

  // http://localhost:8080/api/courses/
  @GetMapping
  @Operation(summary = "Find All Course Details")
  public ResponseEntity<List<Course>> getAllCourses() {
    Optional<List<Course>> courses = service.findAll();

    return courses.map(courseDetails -> new ResponseEntity<>(courseDetails, HttpStatus.OK))
            .orElseThrow(() -> new CourseNotFoundException("No Courses are available.."));
  }

  // http://localhost:8080/api/courses/course-titles?title=boot
  @GetMapping("/course-titles")
  @Operation(summary = "Find courses By title")
  public ResponseEntity<List<Course>> getAllCoursesBasedOnTitle(@RequestParam String title) {
    Optional<List<Course>> courses = service.findByTitleContaining(title);

    return courses.map(courseDetails -> new ResponseEntity<>(courseDetails, HttpStatus.OK))
            .orElseThrow(() -> new CourseNotFoundException("No Courses are available.."));
  }

  // http://localhost:8080/api/courses/1
  @GetMapping("/{id}")
  @Operation(summary = "Find Course By Id")
  public ResponseEntity<Course> getCourseById(@PathVariable("id") String id) {
    Optional<Course> course = service.findById(id);

    return course.map(courseOne -> new ResponseEntity<>(courseOne, HttpStatus.OK))
            .orElseThrow(() -> new CourseNotFoundException("No Courses are available.."));

  }

  // http://localhost:8080/api/courses
  @PostMapping
  @Operation(summary = "Create a New Course")
  public ResponseEntity<Course> createCourse(@RequestBody Course course) {
    Optional<Course> newCourse = service.createCourse(course);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(newCourse.get().getId())
            .toUri();

    return ResponseEntity.created(location)
            .build();
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update Course By Id")
  public ResponseEntity<Optional<Course>> updateCourse(@PathVariable("id") String id,
                                                       @RequestBody Course course) {
    var courseData = service.findById(id);

    if (courseData.isPresent()) {
      Course updateCourse = courseData.get();
      updateCourse.setTitle(course.getTitle());
      updateCourse.setDescription(course.getDescription());
      updateCourse.setPublished(course.isPublished());
      return new ResponseEntity<>(service.createCourse(updateCourse), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping
  @Operation(summary = "Delete All Courses")
  public ResponseEntity<HttpStatus> deleteAllCourses() {
    service.deleteAllCourses();
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete Course By Id")
  public ResponseEntity<HttpStatus> deleteCourseById(@PathVariable("id") String id) {
    service.deleteCourseById(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}
```

## Run & Test

Run Spring Boot application with command: `mvn spring-boot:run`

> Get All courses: http://localhost:8080/api/courses/

> Get A Single Course: http://localhost:8080/api/courses/1

> Get All course based on the title: http://localhost:8080/api/courses/course-titles?title=boot

> Create a new course: http://localhost:8080/api/courses

**Sample POST Request**

```json
{
  "title": "test title",
  "description": "testdescription",
  "published": false
}
```

> Delete All courses: http://localhost:8080/api/courses/

> Delete A Single Course: http://localhost:8080/api/courses/1

> Update A Single Course: http://localhost:8080/api/courses/1

**Sample PUT Request**

```json
{
  "title": "Updated test title",
  "description": "Updated test description",
  "published": true
}
```

## Create Dockerfile for Spring Boot App

Create .Dockerfile in the root folder

```properties
FROM maven:3.8.5-openjdk-17

WORKDIR /spring-boot-app-with-mongodb
COPY . .
RUN mvn clean install -DskipTests

CMD mvn spring-boot:run
```

- FROM: install the image of the Maven – JDK version.
- WORKDIR: path of the working directory.
- COPY: copy all the files inside the project directory to the container.
- RUN: execute a command-line inside the container: mvn clean install -DskipTests to install the dependencies
  in `pom.xml`.
- CMD: run script `mvn spring-boot:run` after the image is built.

## Write Docker Compose configurations

On the root of the project directory, we'll create thecompose.yml file.

Follow version 3 syntax defined by Docker:

```yaml
version: '3.8'

services:
  course-app:
  mongo_db:

```

`version`: Docker Compose file format version will be used.
`services`: individual services in isolated containers.

Our application has two services: `course-app` (Spring Boot) and `mongo_db` (MONGODB database).

Here go with the complete docker compose file

```yaml
version: '3.8'

services:
  course-app:
    depends_on:
      - mongo_db
    image: spring-boot-app-with-mongodb
    build:
      context: .
      dockerfile: Dockerfile
    env_file: ./.env
    ports:
      - $SPRING_LOCAL_PORT:$SPRING_DOCKER_PORT
    environment:
      SPRING_APPLICATION_JSON: '{
            "spring.data.mongodb.uri" : "mongodb://$MONGODB_USER:$MONGODB_PASSWORD@mongo_db:$MONGODB_DOCKER_PORT/$MONGODB_DATABASE?authSource=admin"
          }'
    volumes:
      - .m2:/root/.m2
    stdin_open: true
    tty: true
  mongo_db:
    image: mongo:5.0.2
    env_file: ./.env
    environment:
      - MONGO_INITDB_ROOT_USERNAME=$MONGODB_USER
      - MONGO_INITDB_ROOT_PASSWORD=$MONGODB_PASSWORD
    ports:
      - $MONGODB_LOCAL_PORT:$MONGODB_DOCKER_PORT
    volumes:
      - db:/data/db

volumes:
  db:
```

## Components of Docker Compose File

- course-app:
    - `image`: final image name
    - `build`: configuration options that are applied at build time that we defined in the _Dockerfile_ with relative
      path
    - `environment`: environmental variables that Spring Boot application uses
    - `ports` : Inbound and outbound ports
    - `depends_on`: dependency order, `mongo_db` is started before app
- mongo_db:
    - `image`: mongodb image pull it from docker hub registry, if it is not available in the locally
    - `env_file`: specify our .env path that we will create later
    - `environment`: Specify the environment variables
    - `ports`: Inbound and outbound ports

## Docker Compose Environment variables

In the service configuration, we utilized environmental variables specified within the `.env` file.
Now we will create it.

### _.env_

> _**NOTE: REPLACE USERNAME AND PASSWORD AS PER YOUR NEED**_

```properties
MONGODB_USER=root
MONGODB_PASSWORD=123456
MONGODB_DATABASE=course_db
MONGODB_LOCAL_PORT=7017
MONGODB_DOCKER_PORT=27017

SPRING_LOCAL_PORT=8080
SPRING_DOCKER_PORT=8080
```

## Run the Spring Boot microservice with Docker Compose

> NOTE: Before executing the Docker Compose command, it is imperative to initiate the Docker Desktop software.**

To spin up the containers for both course-app and mongodb database, execute the docker compose command given below.

> docker compose up

Docker will pull the mongodb and Maven images (if our machine does not have it before).

The services can be run on the background with command:

> docker compose up -d

```logsyaml
puneethsai@Puneeths-MacBook-Pro spring-boot-app-with-mongodb % docker compose up -d
[+] Running 1/1
 ! course-app Warning                                                                                                                                                                                3.6s 
[+] Building 64.3s (9/9) FINISHED                                                                                                                                                    docker:desktop-linux
 => [course-app internal] load .dockerignore                                                                                                                                                         0.0s
 => => transferring context: 680B                                                                                                                                                                    0.0s
 => [course-app internal] load build definition from Dockerfile                                                                                                                                      0.0s
 => => transferring dockerfile: 173B                                                                                                                                                                 0.0s
 => [course-app internal] load metadata for docker.io/library/maven:3.8.5-openjdk-17                                                                                                                 0.9s
 => CACHED [course-app 1/4] FROM docker.io/library/maven:3.8.5-openjdk-17@sha256:3a9c30b3af6278a8ae0007d3a3bf00fff80ec3ed7ae4eb9bfa1772853101549b                                                    0.0s
 => [course-app internal] load build context                                                                                                                                                         0.1s
 => => transferring context: 12.77kB                                                                                                                                                                 0.0s
 => [course-app 2/4] WORKDIR /spring-boot-app-with-mongodb                                                                                                                                           0.0s
 => [course-app 3/4] COPY . .                                                                                                                                                                        0.5s
 => [course-app 4/4] RUN mvn clean install -DskipTests                                                                                                                                              61.6s
 => [course-app] exporting to image                                                                                                                                                                  1.2s
 => => exporting layers                                                                                                                                                                              1.2s
 => => writing image sha256:4e4d9371279db40c75366e6b0da6bb75161380fb8f77e91dfb4094dd3663b848                                                                                                         0.0s
 => => naming to docker.io/library/spring-boot-app-with-mongodb                                                                                                                                      0.0s
[+] Running 4/4
 ✔ Network spring-boot-app-with-mongodb_default         Created                                                                                                                                      0.1s 
 ✔ Volume "spring-boot-app-with-mongodb_db"             Created                                                                                                                                      0.0s 
 ✔ Container spring-boot-app-with-mongodb-mongo_db-1    Started                                                                                                                                      0.1s 
 ✔ Container spring-boot-app-with-mongodb-course-app-1  Started                                                                                                                                      0.1s                                                                                                                                     0.1s 
```

## Containers Running in Docker Desktop

![Image](./images/courseappindocker.png "Docker Desktop")

## Run & Test

Using OpenAPI Documentation, we'll be able to access all the operations, please access the below URL

> http://localhost:8080/swagger-ui/index.html

![Image](./images/swagger.png "Swagger OpenAPI")