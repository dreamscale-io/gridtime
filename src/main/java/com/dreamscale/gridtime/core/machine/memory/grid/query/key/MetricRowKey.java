package com.dreamscale.gridtime.core.machine.memory.grid.query.key;

import com.dreamscale.gridtime.api.grid.Observable;
import lombok.Getter;

@Getter
public enum MetricRowKey implements Observable, Key {
    EXECUTION_RUN_TIME("@exec/runtime"),
    EXECUTION_CYCLE_TIME("@exec/cycletime"),
    FILE_TRAVERSAL_VELOCITY("@nav/speed"),
    MODIFICATION_COUNT("@nav/modify"),
    FOCUS_WEIGHT("@nav/focus"),
    FEELS("@flow/feels"),
    IS_WTF("@flow/wtf"),
    FLOW_MODS("@flow/modify"),
    IS_LEARNING("@flow/learn"),
    IS_PROGRESS("@flow/prog"),
    IS_PAIRING("@auth/pair"),

    ZOOM_DURATION_IN_TILE("@zoom/time"),
    ZOOM_AVG_FLAME("@zoom/feels"),
    ZOOM_PERCENT_WTF("@zoom/wtf"),
    ZOOM_PERCENT_LEARNING("@zoom/learn"),
    ZOOM_PERCENT_PAIRING("@zoom/pair"),
    ZOOM_PERCENT_PROGRESS("@zoom/prog");

    private String name;

    MetricRowKey(String name) {
        this.name = name;
    }

    @Override
    public String toDisplayString() {
        return name;
    }

    public String toString() {
        return toDisplayString();
    }
}
