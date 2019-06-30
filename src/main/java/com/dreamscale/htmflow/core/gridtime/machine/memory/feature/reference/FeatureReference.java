package com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Observable;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.FeatureType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.FeatureDetails;
import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class FeatureReference implements Cloneable, Observable {


    //treat as final, but using clone to clone child implementation classes,
    // then setting variables during construction
    private UUID featureId;
    private FeatureType featureType;
    private String searchKey;

    private FeatureDetails details;

    private boolean isResolved;


    public FeatureReference(UUID featureId, FeatureType featureType, String searchKey, FeatureDetails details, boolean isResolved) {
        this.featureId = featureId;
        this.featureType = featureType;
        this.searchKey = searchKey;
        this.details = details;

        this.isResolved = isResolved;
    }

    public void resolve() {
        this.isResolved = true;
    }

    public void resolve(UUID id, FeatureDetails details) {
        this.featureId = id;
        this.details = details;
        this.isResolved = true;
    }

    public String getShortId() {
        return featureId.toString().substring(0, 8);
    }

    public abstract String toDisplayString();
}
