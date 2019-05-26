package com.dreamscale.htmflow.core.feeds.pool;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class FeatureReference {

    private final UUID id;
    private final FeatureType featureType;
    private final String searchKey;

    private GridFeature feature;

    private final boolean isResolved;


    public FeatureReference(FeatureType featureType, String searchKey, GridFeature feature) {
        this.id = UUID.randomUUID();
        this.featureType = featureType;
        this.searchKey = searchKey;
        this.feature = feature;

        isResolved = false;
    }

    public FeatureReference resolve() {
        return new FeatureReference(id, featureType, searchKey, feature, true);
    }

    public FeatureReference resolve(UUID id, GridFeature feature) {
        return new FeatureReference(id, featureType, searchKey, feature, true);
    }

}
