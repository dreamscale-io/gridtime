package com.dreamscale.gridtime.core.machine.memory.tag;

import com.dreamscale.gridtime.api.grid.Observable;

public interface Tag extends Observable {
    String toDisplayString();

    String name();

    String getType();
}
