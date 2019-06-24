package com.dreamscale.htmflow.core.gridtime.machine.memory.type;

import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;

import java.util.Map;

public class TypeRegistry {

    private Map<String, Class<? extends FeatureType>> featureTypeClassMap;

    public TypeRegistry() {
        featureTypeClassMap = DefaultCollections.map();

        registerType(AuthorsType.class);
        registerType(CmdType.class);
        registerType(ExecutionEventType.class);
        registerType(FeelsType.class);
        registerType(IdeaFlowStateType.class);
        registerType(PlaceType.class);
        registerType(WorkContextType.class);
    }

    private void registerType(Class<? extends FeatureType> featureTypeClass) {
        FeatureType[] types = featureTypeClass.getEnumConstants();

        String classType = types[0].getClassType();
        featureTypeClassMap.put(classType, featureTypeClass);
    }

    private FeatureType resolveSubType(Class<? extends FeatureType> featureTypeClass, String typeUri) {
        FeatureType[] types = featureTypeClass.getEnumConstants();
        return types[0].resolveType(typeUri);
    }

    public FeatureType resolveFeatureType(String typeUri) {
        int indexOfFirstSlash = typeUri.indexOf('/');
        String atRoute = typeUri.substring(0, indexOfFirstSlash);
        Class<? extends FeatureType> clazzType = featureTypeClassMap.get(atRoute);

        return resolveSubType(clazzType, typeUri);
    }
}
