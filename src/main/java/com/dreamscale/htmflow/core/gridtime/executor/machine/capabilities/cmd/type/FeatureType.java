package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.Observable;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.FeatureDetails;

import java.util.Map;
import java.util.Set;

public interface FeatureType extends Observable {

    String getClassType();

    String getTypeUri();

    Set<String> getTemplateVariables();

    String expandUri(Map<String, String> templateVariables);

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
}
