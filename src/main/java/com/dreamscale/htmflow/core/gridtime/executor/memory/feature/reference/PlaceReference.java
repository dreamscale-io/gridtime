package com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.PlaceType;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.FeatureDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PlaceReference extends FeatureReference {

    public PlaceReference(PlaceType placeType, String searchKey, FeatureDetails details) {
        super(UUID.randomUUID(), placeType, searchKey, details, false);
    }

    public PlaceType getPlaceType() {
        return (PlaceType)getFeatureType();
    }

    public <T> T getPlaceDetails() {
        return (T)getDetails();
    }

    @Override
    public String toDisplayString() {
        return getPlaceType().toDisplayString();
    }
}
