package com.lmkr.hesco.common.exception;

import com.lmkr.hesco.adminbound.exception.DependentRecordsExistException;
import com.lmkr.hesco.adminbound.exception.InvalidCodeHierarchyException;
import com.lmkr.hesco.common.api.ApiErrorResponse;
import com.lmkr.hesco.reports.exception.MissingReportScopeException;
import com.lmkr.hesco.survey.exception.DuplicateGpsNumberException;
import com.lmkr.hesco.survey.exception.InvalidEquipmentSequenceException;
import com.lmkr.hesco.survey.exception.InvalidSurveyDetailException;
import com.lmkr.hesco.user.exception.RoleBoundMismatchException;
import com.lmkr.hesco.workorder.exception.CreatorScopeViolationException;
import com.lmkr.hesco.workorder.exception.InvalidWorkOrderTransitionException;
import com.lmkr.hesco.workorder.exception.MissingRejectionCommentException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Single place every module's REST layer routes exceptions through — see
 * hesco-api-contract.md for the authoritative status/error-code table this
 * class must stay in sync with. Every business-rule exception thrown by a
 * validator/service (previously enforced by Postgres triggers) is mapped
 * here explicitly; letting one fall through to handleGeneric() and come
 * back as a bare 500 is a bug (see README §2, issue 1).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ================= 400 BAD_REQUEST — business-rule validator rejected input =================
    // (invalid code hierarchy, role/bound mismatch, invalid equipment sequence, missing rejection comment)

    @ExceptionHandler(InvalidCodeHierarchyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleInvalidCodeHierarchy(InvalidCodeHierarchyException ex, HttpServletRequest request) {
        return buildError(ex.getMessage(), "BAD_REQUEST", ex, request);
    }

    @ExceptionHandler(RoleBoundMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleRoleBoundMismatch(RoleBoundMismatchException ex, HttpServletRequest request) {
        return buildError(ex.getMessage(), "BAD_REQUEST", ex, request);
    }

    @ExceptionHandler(InvalidEquipmentSequenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleInvalidEquipmentSequence(InvalidEquipmentSequenceException ex,
                                                             HttpServletRequest request) {
        return buildError(ex.getMessage(), "BAD_REQUEST", ex, request);
    }

    @ExceptionHandler(MissingRejectionCommentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMissingRejectionComment(MissingRejectionCommentException ex,
                                                            HttpServletRequest request) {
        return buildError(ex.getMessage(), "BAD_REQUEST", ex, request);
    }

    @ExceptionHandler(MissingReportScopeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMissingReportScope(MissingReportScopeException ex, HttpServletRequest request) {
        return buildError(ex.getMessage(), "BAD_REQUEST", ex, request);
    }

    // ================= 403 FORBIDDEN — actor not permitted to perform the action =================

    @ExceptionHandler(CreatorScopeViolationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleCreatorScopeViolation(CreatorScopeViolationException ex,
                                                          HttpServletRequest request) {
        return buildError(ex.getMessage(), "FORBIDDEN", ex, request);
    }

    // ================= 404 NOT_FOUND =================

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return buildError(ex.getMessage(), "NOT_FOUND", ex, request);
    }

    // ================= 409 CONFLICT — conflicts with current state =================
    // (dependent records exist, duplicate identifier, illegal state transition)

    @ExceptionHandler(DependentRecordsExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleDependency(DependentRecordsExistException ex, HttpServletRequest request) {
        return buildError(ex.getMessage(), "CONFLICT", ex, request);
    }

    @ExceptionHandler(DuplicateGpsNumberException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleDuplicateGpsNumber(DuplicateGpsNumberException ex, HttpServletRequest request) {
        return buildError(ex.getMessage(), "CONFLICT", ex, request);
    }

    @ExceptionHandler(InvalidWorkOrderTransitionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleInvalidWorkOrderTransition(InvalidWorkOrderTransitionException ex,
                                                               HttpServletRequest request) {
        return buildError(ex.getMessage(), "CONFLICT", ex, request);
    }

    // ================= 400 VALIDATION_ERROR — @Valid field-level failures =================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildError(message, "VALIDATION_ERROR", ex, request);
    }

    // ================= 500 INTERNAL_ERROR — fallback only =================

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGeneric(Exception ex, HttpServletRequest request) {
        return buildError("Unexpected error occurred", "INTERNAL_ERROR", ex, request);
    }

    @ExceptionHandler(InvalidSurveyDetailException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleInvalidSurveyDetail(InvalidSurveyDetailException ex,
                                                      HttpServletRequest request) {
        return buildError(ex.getMessage(), "BAD_REQUEST", ex, request);
    }

    private ApiErrorResponse buildError(String message, String code,Exception ex, HttpServletRequest request) {
        ex.printStackTrace();
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(code)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
    }
}
