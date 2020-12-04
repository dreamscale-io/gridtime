package com.dreamscale.gridtime.api.terminal;

import lombok.Getter;

@Getter
public enum ActivityContext {
    SYSTEM("Grid operating system administration"),
    PEOPLE("Manage teams and send invites"),
    PROJECT("Manage project configs and permissions"),
    TILES("Query grid data tiles");

    private final String description;

    ActivityContext(String description) {
        this.description = description;
    }

    public static ActivityContext fromString(String helpGroup) {
        return ActivityContext.valueOf(helpGroup.toUpperCase());
    }
}
