package com.dreamscale.gridtime.core.exception;

public enum ConflictErrorCodeGroups {

    CONFLICT("CONFLICT");

    private String group;

    ConflictErrorCodeGroups(String group) {
        this.group = group;
    }

    public String makeErrorCode(int subcode) {
        return String.format("%s-%04d", this.group, subcode);
    }

}
