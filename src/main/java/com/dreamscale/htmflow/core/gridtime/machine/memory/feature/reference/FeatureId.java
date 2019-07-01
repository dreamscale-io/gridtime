package com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Observable;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.FeatureDetails;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.FeatureType;
import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class FeatureId implements Cloneable, Observable {

    private UUID featureId;
}
