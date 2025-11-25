package com.springapp.springbootappwithmongodb.exception;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<@NonNull ExceptionResponse> handleAllExceptions(Exception exception,
                                                                                WebRequest request) {
        var exceptionResponse = new ExceptionResponse(LocalDateTime.now(), exception.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public final ResponseEntity<@NonNull ExceptionResponse> handleProductNotFoundException(CourseNotFoundException productNotFoundException,
                                                                                           WebRequest request) {
        var exceptionResponse = new ExceptionResponse(LocalDateTime.now(),
                productNotFoundException.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }
}
