package com.dreamscale.gridtime.core.exception;

public enum ValidationErrorCodeGroups {

    VALIDATION("INVALID");

    private String group;

    ValidationErrorCodeGroups(String group) {
        this.group = group;
    }

    public String makeErrorCode(int subcode) {
        return String.format("%s-%04d", this.group, subcode);
    }

}
