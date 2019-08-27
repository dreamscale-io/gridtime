package com.dreamscale.gridtime.core.machine.memory.tag;

public interface StartTag extends Tag {
    //are there common things about starting?

    //search for tag match

    default String toDisplayString() {
        return "^";
    }

    default String getType() {
        return "Start";
    }

}
