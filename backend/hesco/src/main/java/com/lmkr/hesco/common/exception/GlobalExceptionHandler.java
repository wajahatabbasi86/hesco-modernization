package com.lmkr.hesco.common.exception;

import com.lmkr.hesco.adminbound.exception.DependentRecordsExistException;
import com.lmkr.hesco.common.api.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========================= NOT FOUND =========================
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(ex.getMessage(), "NOT_FOUND", request);
    }

    // ========================= DEPENDENCY EXISTS =========================
    @ExceptionHandler(DependentRecordsExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleDependency(
            DependentRecordsExistException ex,
            HttpServletRequest request
    ) {
        return buildError(ex.getMessage(), "DEPENDENCY_EXISTS", request);
    }

    // ========================= VALIDATION =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildError(message, "VALIDATION_ERROR", request);
    }

    // ========================= GENERIC =========================
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError("Unexpected error occurred", "INTERNAL_ERROR", request);
    }

    // ========================= BUILDER =========================
    private ApiErrorResponse buildError(
            String message,
            String code,
            HttpServletRequest request
    ) {
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(code)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
    }
}