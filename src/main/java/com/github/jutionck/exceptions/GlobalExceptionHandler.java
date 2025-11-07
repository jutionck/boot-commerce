package com.github.jutionck.exceptions;

import com.github.jutionck.dto.response.ErrorResponse;
import com.github.jutionck.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseUtil.buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "Not Found - Resource does not exist",
            List.of(ex.getMessage())
        );
    }

    @ExceptionHandler(ResourceDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleResourceDuplicate(ResourceDuplicateException ex) {
        log.error("Resource duplicate: {}", ex.getMessage());
        return ResponseUtil.buildErrorResponse(
            HttpStatus.CONFLICT,
            "Conflict - Resource already exists",
            List.of(ex.getMessage())
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        log.error("Unauthorized: {}", ex.getMessage());
        return ResponseUtil.buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized - Invalid credentials",
            List.of(ex.getMessage())
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return ResponseUtil.buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Bad Request - Validation failed",
            ex.getErrors()
        );
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex) {
        log.error("Invalid file: {}", ex.getMessage());
        return ResponseUtil.buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Bad Request - Invalid file",
            List.of(ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());

        return ResponseUtil.buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Bad Request - Validation failed",
            errors
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        return ResponseUtil.buildErrorResponse(
            HttpStatus.CONFLICT,
            "Conflict - Data constraint violation",
            List.of("Resource already exists or constraint violation")
        );
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(Exception ex) {
        log.error("Authentication failed: {}", ex.getMessage());
        return ResponseUtil.buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized - Invalid credentials",
            List.of("Invalid email or password")
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return ResponseUtil.buildErrorResponse(
            HttpStatus.FORBIDDEN,
            "Forbidden - Insufficient permissions",
            List.of("You don't have permission to access this resource")
        );
    }

    @ExceptionHandler({LockedException.class, DisabledException.class})
    public ResponseEntity<ErrorResponse> handleAccountLocked(Exception ex) {
        log.error("Account locked/disabled: {}", ex.getMessage());
        return ResponseUtil.buildErrorResponse(
            HttpStatus.FORBIDDEN,
            "Forbidden - Account locked or disabled",
            List.of("Your account is locked or disabled")
        );
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtError(JwtAuthenticationException ex) {
        log.error("JWT error: {}", ex.getMessage());
        return ResponseUtil.buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized - Invalid or missing token",
            List.of("You must provide a valid access token")
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationError(AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return ResponseUtil.buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized - Authentication failed",
            List.of(ex.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseUtil.buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            List.of("An unexpected error occurred")
        );
    }
}
