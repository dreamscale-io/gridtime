package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key;

import lombok.Getter;

@Getter
public enum TrackSetKey implements Key {
    WorkContext("@work"),
    Authors("@author"),
    IdeaFlow("@flow"),
    Feels("@feels"),
    Executions("@exec"),
    Navigations("@nav");

    private final String name;

    TrackSetKey(String name) {
        this.name = name;
    }
}
