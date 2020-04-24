package com.dreamscale.gridtime.core.exception;

import org.dreamscale.exception.ErrorCodes;

import static com.dreamscale.gridtime.core.exception.InternalErrorCodeGroups.INTERNAL;


public enum InternalErrorCodes implements ErrorCodes {

    TALK_TIMEOUT(1, INTERNAL),
    TALK_ERROR(2, INTERNAL);

    private int subcode;
    private InternalErrorCodeGroups errorCodeGroup;

    InternalErrorCodes(int subcode, InternalErrorCodeGroups errorCodeGroup) {
        this.subcode = subcode;
        this.errorCodeGroup = errorCodeGroup;
    }

    @Override
    public String makeErrorCode() {
        return this.errorCodeGroup.makeErrorCode(this.subcode);
    }
}
