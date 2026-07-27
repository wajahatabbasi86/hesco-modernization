package com.lmkr.hesco.survey.exception;

/**
 * Thrown when a survey_form's conditional detail payload (SRS §8.3.3-
 * §8.3.6) is missing when required, present when not applicable, or
 * references an item_type from the wrong item_category — e.g. a
 * poleDetail.structureTypeCode that resolves to an HT_CONDUCTOR item
 * instead of PRIMARY_STRUCTURE/SECONDARY_STRUCTURE. Mapped to 400 in
 * GlobalExceptionHandler, same bucket as every other business-rule
 * validation failure.
 */
public class InvalidSurveyDetailException extends RuntimeException {
    public InvalidSurveyDetailException(String message) {
        super(message);
    }
}