package com.dreamscale.gridtime.core.exception;

public enum InternalErrorCodeGroups {

    INTERNAL("INTERNAL");

    private String group;

    InternalErrorCodeGroups(String group) {
        this.group = group;
    }

    public String makeErrorCode(int subcode) {
        return String.format("%s-%04d", this.group, subcode);
    }

}
