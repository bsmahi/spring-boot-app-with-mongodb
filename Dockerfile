FROM maven:3.9.11-amazoncorretto-25-alpine AS build

WORKDIR /spring-boot-app-with-mongodb
COPY . .
RUN mvn clean install -DskipTests

CMD mvn spring-boot:run