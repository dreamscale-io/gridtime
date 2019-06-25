package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Observable;

public enum MetricType implements Observable {
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

    private String shortHandString;

    MetricType(String shortHandString) {
        this.shortHandString = shortHandString;
    }

    @Override
    public String toDisplayString() {
        return shortHandString;
    }
}
