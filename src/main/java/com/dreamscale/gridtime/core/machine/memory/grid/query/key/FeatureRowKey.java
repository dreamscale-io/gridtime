package com.dreamscale.gridtime.core.machine.memory.grid.query.key;

import lombok.Getter;

@Getter
public enum FeatureRowKey implements Key {

    AUTHOR_NAME("@author/name"),
    EXEC_RHYTHM("@exec/rhythm"),

    FLOW_FEELS("@flow/feels"),
    FLOW_WTF("@flow/wtf"),
    FLOW_LEARNING("@flow/learn"),

    WORK_PROJECT("@work/project"),
    WORK_TASK("@work/task"),
    WORK_INTENTION("@work/intent"),

    NAV_BOX("@nav/box"),
    NAV_BRIDGE("@nav/bridge"),
    NAV_BATCH("@nav/batch"),
    NAV_RHYTHM("@nav/rhythm");

    private final String name;

    FeatureRowKey(String name) {
        this.name = name;
    }
}
