package com.dreamscale.gridtime.api.terminal;

import lombok.Getter;

@Getter
public enum ActivityContext {
    SYSTEM("Grid operating system administration"),
    PEOPLE("Manage teams and send invites"),
    PROJECT("Manage projects"),
    QUERY("Query flow activity data");

    private final String description;

    ActivityContext(String description) {
        this.description = description;
    }

    public static ActivityContext fromString(String helpGroup) {
        return ActivityContext.valueOf(helpGroup.toUpperCase());
    }
}
