package com.dreamscale.gridtime.core.machine.memory.feature.details;

public enum StructureLevel {
    INTENTION(1), TASK(2), PROJECT(3);

    private final int level;

    StructureLevel(int level) {
        this.level = level;
    }

    public StructureLevel getInsideStructureLevel() {
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
