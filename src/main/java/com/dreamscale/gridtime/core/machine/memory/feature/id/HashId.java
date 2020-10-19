package com.dreamscale.gridtime.core.machine.memory.feature.id;

import com.dreamscale.gridtime.api.grid.Observable;

import java.util.UUID;

class HashId implements Observable {

    private final UUID id;
    private final String shortString;

    public HashId(UUID id) {
        this.id = id;
        this.shortString = id.toString().substring(0, 8);
    }

    public String toDisplayString() {
        return shortString;
    }

    public UUID toUUID() {
        return id;
    }

}
