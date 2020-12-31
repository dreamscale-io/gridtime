package com.dreamscale.gridtime.core.machine.memory.feature.reference;

import com.dreamscale.gridtime.core.machine.memory.feature.details.FeatureDetails;
import com.dreamscale.gridtime.core.machine.memory.type.FeelsType;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeelsRatingDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FeelsReference extends FeatureReference {

    public FeelsReference(FeelsType feelsType, String searchKey, FeelsRatingDetails feelsDetails) {
        super(UUID.randomUUID(), feelsType, searchKey, feelsDetails, false);
    }

    public FeelsType getFeelsType() {
        return (FeelsType) getFeatureType();
    }

    public Integer getFlameRating() {
        return ((FeelsRatingDetails) getDetails()).getRating();
    }

    @Override
    public void resolve(UUID id, FeatureDetails details) {
        if (details == null) {
            throw new RuntimeException("Resolving feels reference witthout details");
        }

        super.resolve(id, details);
    }

    @Override
    public String toDisplayString() {
        return ((FeelsRatingDetails) getDetails()).toDisplayString();
    }

    @Override
    public String getDescription() {
        return "Feels "+getFlameRating();
    }
}
