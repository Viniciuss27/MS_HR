package vinix.resources.exceptions;

import java.time.Instant;

import org.springframework.http.HttpStatus;        //Spring, não Apache
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import vinix.services.exceptions.DatabaseException;
import vinix.services.exceptions.ResourceNotFoundException;

@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> resourceNotFound(
            ResourceNotFoundException e,
            HttpServletRequest request) {

        String error = "Resource not found";
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandardError err = new StandardError(
            Instant.now(),
            status.value(),
            error,
            e.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<StandardError> database(
            DatabaseException e,
            HttpServletRequest request) {

        String error = "Database error";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandardError err = new StandardError(
            Instant.now(),
            status.value(),
            error,
            e.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }
}