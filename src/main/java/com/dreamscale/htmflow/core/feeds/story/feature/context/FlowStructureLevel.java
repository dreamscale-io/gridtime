package com.dreamscale.htmflow.core.feeds.story.feature.context;

public enum FlowStructureLevel {
    INTENTION(1), TASK(2), PROJECT(3);

    private final int level;

    FlowStructureLevel(int level) {
        this.level = level;
    }

    public FlowStructureLevel getInsideStructureLevel() {
        switch (this) {
            case INTENTION:
                return null;
            case TASK:
                return INTENTION;
            case PROJECT:
                return TASK;
        }
        return null;
    }

}
