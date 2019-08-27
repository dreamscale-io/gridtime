package com.dreamscale.gridtime.core.exception;

import org.dreamscale.exception.ErrorCodes;

import static com.dreamscale.gridtime.core.exception.ValidationErrorCodeGroups.VALIDATION;


public enum ValidationErrorCodes implements ErrorCodes {

    MISSING_OR_INVALID_ORGANIZATION(1, VALIDATION),
    MISSING_OR_INVALID_JIRA_PROJECT(2, VALIDATION),
    MISSING_OR_INVALID_JIRA_USER(3, VALIDATION),
    NO_ORG_MEMBERSHIP_FOR_ACCOUNT(4, VALIDATION),
    INVALID_PROJECT_REFERENCE(5, VALIDATION),
    INVALID_OR_EXPIRED_ACTIVATION_CODE(6, VALIDATION),
    INVALID_OR_EXPIRED_INVITE_TOKEN(7, VALIDATION),
    MISSING_OR_INVALID_TEAM(8, VALIDATION),
    NO_INPUTS_PROVIDED(9, VALIDATION),
    INVALID_SEARCH_STRING(10, VALIDATION),
    INVALID_FINISH_STATUS(11, VALIDATION),

    FAILED_PUBLICKEY_GENERATION(12, VALIDATION),
    NO_ACCESS_TO_CIRCLE_KEY(13, VALIDATION),
    CHANNEL_NOT_FOUND(14, VALIDATION),
    CHANNEL_ACCESS_DENIED(15, VALIDATION);


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
