package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Observable;
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
    IS_PAIRING("@auth/pair");

    private String name;

    MetricRowKey(String name) {
        this.name = name;
    }

    @Override
    public String toDisplayString() {
        return name;
    }
}