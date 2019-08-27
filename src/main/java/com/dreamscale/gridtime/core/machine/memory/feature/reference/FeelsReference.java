package com.dreamscale.gridtime.core.machine.memory.feature.reference;

import com.dreamscale.gridtime.core.machine.memory.type.FeelsType;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeelsRatingDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FeelsReference extends FeatureReference {

    public FeelsReference(FeelsType feelsType, String searchKey) {
        super(UUID.randomUUID(), feelsType, searchKey, null, false);
    }

    public FeelsReference(FeelsType feelsType, String searchKey, FeelsRatingDetails feelsDetails) {
        super(UUID.randomUUID(), feelsType, searchKey, feelsDetails, false);
    }

    public FeelsType getFeelsType() {
        return (FeelsType) getFeatureType();
    }

    public int getFlameRating() {
        return ((FeelsRatingDetails) getDetails()).getRating();
    }

    @Override
    public String toDisplayString() {
        return ((FeelsRatingDetails) getDetails()).toDisplayString();
    }
}
