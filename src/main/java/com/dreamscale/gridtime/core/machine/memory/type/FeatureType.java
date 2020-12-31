package com.dreamscale.gridtime.core.machine.memory.type;

import com.dreamscale.gridtime.api.grid.Observable;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeatureDetails;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;

import java.util.Map;
import java.util.Set;

public interface FeatureType extends Observable {

    String getClassType();

    String getTypeUri();

    Set<String> getTemplateVariables();

    String expandUri(Map<String, String> templateVariables);

    Map<String, String> parseUri(String uri);

    Class<? extends FeatureDetails> getSerializationClass();

    String name();

    default FeatureType resolveType(String typeUri) {
        for (FeatureType type : getClass().getEnumConstants()) {
            if (typeUri.equals(type.getTypeUri())) {
                return type;
            }
        }
        return null;
    }

    FeatureReference createReference(String searchKey, FeatureDetails details);
}
