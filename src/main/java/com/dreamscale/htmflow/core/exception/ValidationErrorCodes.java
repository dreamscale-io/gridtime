package com.dreamscale.htmflow.core.exception;

import org.dreamscale.exception.ErrorCodes;

import static com.dreamscale.htmflow.core.exception.ValidationErrorCodeGroups.VALIDATION;


public enum ValidationErrorCodes implements ErrorCodes {

    MISSING_OR_INVALID_ORGANIZATION(1, VALIDATION),
    MISSING_OR_INVALID_JIRA_PROJECT(2, VALIDATION),
    MISSING_OR_INVALID_JIRA_USER(3, VALIDATION);

    private int subcode;
    private ValidationErrorCodeGroups errorCodeGroup;

    ValidationErrorCodes(int subcode, ValidationErrorCodeGroups errorCodeGroup) {
        this.subcode = subcode;
        this.errorCodeGroup = errorCodeGroup;
    }

    @Override
    public String makeErrorCode() {
        return this.errorCodeGroup.makeErrorCode(this.subcode);
    }
}
