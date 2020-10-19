package com.dreamscale.gridtime.core.machine.memory.feature.reference;

import com.dreamscale.gridtime.api.grid.Observable;
import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class FeatureId implements Cloneable, Observable {

    private UUID featureId;
}
