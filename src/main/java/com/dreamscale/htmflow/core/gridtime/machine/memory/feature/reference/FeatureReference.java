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
    private UUID id;
    private String shortId;
    private FeatureType featureType;
    private String searchKey;

    private FeatureDetails details;

    private boolean isResolved;


    public FeatureReference(UUID id, FeatureType featureType, String searchKey, FeatureDetails details, boolean isResolved) {
        this.id = id;
        this.shortId =  id.toString().substring(0, 8);
        this.featureType = featureType;
        this.searchKey = searchKey;
        this.details = details;

        this.isResolved = isResolved;
    }

    public FeatureReference resolve() {
        FeatureReference resolved = null;

        try {
            resolved = (FeatureReference) clone();
            resolved.isResolved = true;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return resolved;
    }

    public FeatureReference resolve(UUID id, FeatureDetails details) {
        FeatureReference resolved = null;

        try {
            resolved = (FeatureReference) clone();
            resolved.id = id;
            resolved.details = details;
            resolved.isResolved = true;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return resolved;
    }

    public abstract String toDisplayString();
}
