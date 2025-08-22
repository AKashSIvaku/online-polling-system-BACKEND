package com.pollsystem.springapp.exception;

import com.pollsystem.springapp.dto.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MessageResponse> handleBadCredentialsException(
            BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Invalid email or password"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponse> handleAccessDeniedException(
            AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse("Access denied"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGenericException(
            Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("An error occurred: " + ex.getMessage()));
    }
}