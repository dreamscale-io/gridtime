package com.dreamscale.gridtime.core.machine.memory.type;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.details.CircleDetails;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeatureDetails;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public enum IdeaFlowStateType implements FeatureType {

    WTF_STATE("/wtf", "/wtf/{circleId}", CircleDetails.class),
    LEARNING_STATE("/learn", "/learn", null),
    PROGRESS_STATE("/progress", "/progress", null);

    private static final String CLASS_TYPE = "@flow";

    private static final LinkedHashSet<String> TEMPLATE_VARIABLES = DefaultCollections.toSet(TemplateVariable.CIRCLE_ID);

    private final Class<? extends FeatureDetails> serializationClass;
    private final UriTemplate uriTemplate;
    private final String typeUri;

    IdeaFlowStateType(String typeUri, String uriTemplatePath, Class<? extends FeatureDetails> serializationClass) {
        this.typeUri = CLASS_TYPE + typeUri;
        this.uriTemplate = new UriTemplate(CLASS_TYPE + uriTemplatePath);
        this.serializationClass = serializationClass;
    }

    @Override
    public String getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public Set<String> getTemplateVariables() {
        return TEMPLATE_VARIABLES;
    }

    @Override
    public String expandUri(Map<String, String> templateVariables) {
        if (templateVariables != null && !templateVariables.isEmpty()) {
            return uriTemplate.expand(templateVariables).toString();
        } else {
            return typeUri;
        }
    }

    @Override
    public Map<String, String> parseUri(String uri) {
        return uriTemplate.match(uri);
    }


    public String getTypeUri() {
        return typeUri;
    }


    @Override
    public Class<? extends FeatureDetails> getSerializationClass() {
        return serializationClass;
    }

    @Override
    public String toDisplayString() {
        return typeUri;
    }

    public static class TemplateVariable {
        public static String CIRCLE_ID = "circleId";
    }
}
