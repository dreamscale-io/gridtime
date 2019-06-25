package com.dreamscale.htmflow.core.gridtime.kernel.memory.tag;

public interface FinishTag extends Tag {

    default String toDisplayString() {
        return "$";
    }
}
