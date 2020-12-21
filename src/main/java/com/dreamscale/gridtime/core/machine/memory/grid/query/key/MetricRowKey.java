package com.dreamscale.gridtime.core.machine.memory.grid.query.key;

import com.dreamscale.gridtime.api.grid.Observable;
import lombok.Getter;

@Getter
public enum MetricRowKey implements Observable, Key {
    EXECUTION_RUN_TIME("@exec/runtime", false),
    EXECUTION_CYCLE_TIME("@exec/cycletime", false),
    FILE_TRAVERSAL_VELOCITY("@nav/speed", false),
    MODIFICATION_COUNT("@nav/modify", false),
    FOCUS_WEIGHT("@nav/focus", false),
    FEELS("@flow/feels", true),
    IS_WTF("@flow/wtf", false),
    FLOW_MODS("@flow/modify", false),
    IS_LEARNING("@flow/learn", false),
    IS_PROGRESS("@flow/prog", false),
    IS_PAIRING("@auth/pair", false),

    ZOOM_DURATION_IN_TILE("@zoom/time", true),
    ZOOM_AVG_FLAME("@zoom/feels", true),
    ZOOM_PERCENT_WTF("@zoom/wtf", true),
    ZOOM_PERCENT_LEARNING("@zoom/learn", true),
    ZOOM_PERCENT_PAIRING("@zoom/pair", true),
    ZOOM_PERCENT_PROGRESS("@zoom/prog", true);

    private final String name;
    private final boolean required;

    MetricRowKey(String name, boolean isRequired) {
        this.name = name;
        this.required = isRequired;
    }

    @Override
    public String toDisplayString() {
        return name;
    }

    public String toString() {
        return toDisplayString();
    }
}
