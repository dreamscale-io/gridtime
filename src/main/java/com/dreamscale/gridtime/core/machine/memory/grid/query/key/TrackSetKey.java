package com.dreamscale.gridtime.core.machine.memory.grid.query.key;

import lombok.Getter;

@Getter
public enum TrackSetKey implements Key {
    WorkContext("@work"),
    Authors("@author"),
    IdeaFlow("@flow"),
    Executions("@exec"),
    Navigations("@nav");

    private final String name;
    private final boolean required = true;

    TrackSetKey(String name) {
        this.name = name;
    }
}
