package com.dreamscale.gridtime.core.machine.memory.type;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeatureDetails;
import com.dreamscale.gridtime.core.machine.memory.feature.details.WorkContext;
import com.dreamscale.gridtime.core.machine.memory.feature.details.StructureLevel;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public enum WorkContextType implements FeatureType {

    PROJECT_WORK("/project", "/project/{sha}", WorkContext.class),
    TASK_WORK("/task", "/task/{sha}", WorkContext.class),
    INTENTION_WORK("/intention", "/intention/{sha}", WorkContext.class);

    //each option stuff

    private final Class<? extends FeatureDetails> serializationClass;
    private final UriTemplate uriTemplate;
    private final String typeUri;

    //static class level stuff

    private static final String CLASS_TYPE = "@work";

    private static final LinkedHashSet<String> TEMPLATE_VARIABLES = DefaultCollections.toSet(TemplateVariable.SHA);


    WorkContextType(String typeUri, String uriTemplatePath, Class<? extends FeatureDetails> serializationClass) {
        this.typeUri = CLASS_TYPE + typeUri;
        this.uriTemplate = new UriTemplate(CLASS_TYPE + uriTemplatePath);
        this.serializationClass = serializationClass;
    }

    @Override
    public String getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public String getTypeUri() {
        return typeUri;
    }

    @Override
    public Set<String> getTemplateVariables() {
        return TEMPLATE_VARIABLES;
    }

    @Override
    public String expandUri(Map<String, String> templateVariables) {
        return uriTemplate.expand(templateVariables).toString();
    }

    @Override
    public Map<String, String> parseUri(String uri) {
        return uriTemplate.match(uri);
    }

    @Override
    public Class<? extends FeatureDetails> getSerializationClass() {
        return serializationClass;
    }


    public static WorkContextType fromLevel(StructureLevel structureLevel) {
        switch (structureLevel) {
            case PROJECT:
                return PROJECT_WORK;
            case TASK:
                return TASK_WORK;
            case INTENTION:
                return INTENTION_WORK;
        }
        return null;
    }

    @Override
    public String toDisplayString() {
        return typeUri;
    }

    public static class TemplateVariable {
        public static String SHA = "sha";
    }

}
