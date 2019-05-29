package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag;

public interface StartTag extends Tag {
    //are there common things about starting?

    //search for tag match

    default String toDisplayString() {
        return "^";
    }
}
