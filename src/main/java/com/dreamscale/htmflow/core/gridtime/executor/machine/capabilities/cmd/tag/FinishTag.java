package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag;

public interface FinishTag extends Tag {

    default String toDisplayString() {
        return "$";
    }
}
