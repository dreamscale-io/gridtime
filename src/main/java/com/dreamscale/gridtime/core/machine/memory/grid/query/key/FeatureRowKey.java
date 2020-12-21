package com.dreamscale.gridtime.core.machine.memory.grid.query.key;

import lombok.Getter;

@Getter
public enum FeatureRowKey implements Key {

    AUTHOR_NAME("@author/name", true),

    FLOW_FEELS("@flow/feels", true),
    FLOW_WTF("@flow/wtf", true),
    FLOW_LEARNING("@flow/learn", true),

    WORK_PROJECT("@work/project", true),
    WORK_TASK("@work/task", true),
    WORK_INTENTION("@work/intent", true),

    NAV_BOX("@nav/box", false),
    NAV_BRIDGE("@nav/bridge", false),
    NAV_BATCH("@nav/batch", false),
    NAV_RHYTHM("@nav/rhythm", false),

    EXEC_RHYTHM("@exec/rhythm", false);

    private final String name;
    private final boolean required;

    FeatureRowKey(String name, boolean isRequired) {
        this.name = name;
        this.required = isRequired;
    }
}
