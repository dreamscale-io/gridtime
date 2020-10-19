package com.dreamscale.gridtime.api.grid;

import lombok.Getter;

@Getter
public enum GridStatus {
    RUNNING("Gridtime is running."), STOPPED("Gridtime is stopped.");

    private final String message;

    GridStatus(String message) {
        this.message = message;
    }



}
