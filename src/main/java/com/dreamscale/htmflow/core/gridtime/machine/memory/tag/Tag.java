package com.dreamscale.htmflow.core.gridtime.machine.memory.tag;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Observable;

public interface Tag extends Observable {
    String toDisplayString();

    String name();

    String getType();
}