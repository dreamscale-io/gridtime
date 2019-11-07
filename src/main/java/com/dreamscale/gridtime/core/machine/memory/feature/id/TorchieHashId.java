package com.dreamscale.gridtime.core.machine.memory.feature.id;

import java.util.UUID;

public class TorchieHashId extends HashId {

    public TorchieHashId(UUID id) {
        super(id);
    }

    @Override
    public String toDisplayString() {
        return "/torchie/"+super.toDisplayString();
    }

}
