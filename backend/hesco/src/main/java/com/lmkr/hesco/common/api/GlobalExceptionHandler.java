package com.lmkr.hesco.common.api;

import com.lmkr.hesco.adminbound.exception.DependentRecordsExistException;
import com.lmkr.hesco.adminbound.exception.InvalidCodeHierarchyException;
import com.lmkr.hesco.survey.exception.DuplicateGpsNumberException;
import com.lmkr.hesco.survey.exception.InvalidEquipmentSequenceException;
import com.lmkr.hesco.user.exception.RoleBoundMismatchException;
import com.lmkr.hesco.workorder.exception.CreatorScopeViolationException;
import com.lmkr.hesco.workorder.exception.InvalidWorkOrderTransitionException;
import com.lmkr.hesco.workorder.exception.MissingRejectionCommentException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Single place every module's api layer relies on to turn a domain
 * exception into a 4xx with a readable message, instead of each
 * controller writing its own try/catch. New domain exceptions get one
 * line added here.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - malformed/invalid input (business-rule validation failures)
    @ExceptionHandler({
        InvalidCodeHierarchyException.class,
        RoleBoundMismatchException.class,
        InvalidEquipmentSequenceException.class,
        MissingRejectionCommentException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ApiError.of("BAD_REQUEST", ex.getMessage()));
    }

    // 403 - the actor is not allowed to do this (scope/authorization rules)
    @ExceptionHandler({
        CreatorScopeViolationException.class
    })
    public ResponseEntity<ApiError> handleForbidden(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiError.of("FORBIDDEN", ex.getMessage()));
    }

    // 404 - referenced entity does not exist
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError.of("NOT_FOUND", ex.getMessage()));
    }

    // 409 - the request conflicts with current state (dependents exist,
    // duplicate GPS number, illegal work-order transition)
    @ExceptionHandler({
        DependentRecordsExistException.class,
        DuplicateGpsNumberException.class,
        InvalidWorkOrderTransitionException.class
    })
    public ResponseEntity<ApiError> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError.of("CONFLICT", ex.getMessage()));
    }

    // 400 - @Valid DTO field validation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", message));
    }

    // 500 - fallback, never leak a raw stack trace to the client
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        return ResponseEntity.internalServerError()
            .body(ApiError.of("INTERNAL_ERROR", "Unexpected error: " + ex.getMessage()));
    }
}
