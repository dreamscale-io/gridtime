package com.dreamscale.gridtime.core.exception;

import org.dreamscale.exception.ErrorCodes;

import static com.dreamscale.gridtime.core.exception.ConflictErrorCodeGroups.CONFLICT;


public enum ConflictErrorCodes implements ErrorCodes {

    CONFLICTING_CIRCUIT_NAME(1, CONFLICT),
    RETRO_ALREADY_STARTED(2, CONFLICT),
    CIRCUIT_IN_WRONG_STATE(3, CONFLICT),
    CONFLICTING_TEAM_NAME(4, CONFLICT),
    CONFLICTING_USER_NAME(5, CONFLICT),
    EMAIL_ALREADY_IN_USE(6, CONFLICT),
    ORG_DOMAIN_ALREADY_IN_USE(7, CONFLICT),
    NO_SEATS_AVAILABLE(8, CONFLICT),
    CONFLICTING_ACTIVE_CIRCUIT(9, CONFLICT);

    private int subcode;
    private ConflictErrorCodeGroups errorCodeGroup;

    ConflictErrorCodes(int subcode, ConflictErrorCodeGroups errorCodeGroup) {
        this.subcode = subcode;
        this.errorCodeGroup = errorCodeGroup;
    }

    @Override
    public String makeErrorCode() {
        return this.errorCodeGroup.makeErrorCode(this.subcode);
    }
}
