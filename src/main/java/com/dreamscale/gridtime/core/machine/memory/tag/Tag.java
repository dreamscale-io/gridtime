package com.dreamscale.gridtime.core.machine.memory.tag;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Observable;

public interface Tag extends Observable {
    String toDisplayString();

    String name();

    String getType();
}
