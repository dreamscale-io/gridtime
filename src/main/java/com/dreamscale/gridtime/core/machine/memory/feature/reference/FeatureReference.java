package com.dreamscale.gridtime.core.machine.memory.feature.reference;

import com.dreamscale.gridtime.api.grid.Observable;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeatureDetails;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
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

    public abstract String getDescription();

    @Override
    public boolean equals(Object o) {
        if (o instanceof FeatureReference) {
            FeatureReference oReference = (FeatureReference) o;
            if ( oReference == this || oReference.getSearchKey().equals(getSearchKey())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getSearchKey().hashCode();
    }

    @Override
    public String toString() {
        return toDisplayString() + " "+ featureId;
    }
}
