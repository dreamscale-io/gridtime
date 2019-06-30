package com.dreamscale.htmflow.core.gridtime.machine.memory.tag;

public interface FinishTag extends Tag {

    default String toDisplayString() {
        return "$";
    }

    default String getType() {
        return "Finish";
    }
}
