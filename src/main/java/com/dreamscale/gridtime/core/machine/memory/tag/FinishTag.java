package com.dreamscale.gridtime.core.machine.memory.tag;

public interface FinishTag extends Tag {

    default String toDisplayString() {
        return "$";
    }

    default String getType() {
        return "Finish";
    }
}
