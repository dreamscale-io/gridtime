package com.dreamscale.gridtime.api.terminal;

import lombok.Getter;

@Getter
public enum ActivityContext {
    GRID("Operate the grid"),
    INVITE("Send invites"),
    PROJECT("Manage projects");

    private final String description;

    ActivityContext(String description) {
        this.description = description;
    }

    public static ActivityContext fromString(String helpGroup) {
        return ActivityContext.valueOf(helpGroup.toUpperCase());
    }
}
