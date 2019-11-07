package com.dreamscale.gridtime.core.machine.memory.feature.id;

import java.util.UUID;

public class TeamHashId extends HashId {

    public TeamHashId(UUID id) {
        super(id);
    }

    @Override
    public String toDisplayString() {
        return "/team/"+super.toDisplayString();
    }

}
