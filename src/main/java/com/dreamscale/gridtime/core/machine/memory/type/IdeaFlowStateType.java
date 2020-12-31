package com.dreamscale.gridtime.core.machine.memory.type;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.details.CircuitDetails;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeatureDetails;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.ExecutionReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.IdeaFlowStateReference;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public enum IdeaFlowStateType implements FeatureType {

    WTF_STATE("/wtf", "/wtf/{circuitId}", CircuitDetails.class),
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
    public FeatureReference createReference(String searchKey, FeatureDetails details) {
        return new IdeaFlowStateReference(this, searchKey, (CircuitDetails) details);
    }

    @Override
    public String toDisplayString() {
        return typeUri;
    }

    public static class TemplateVariable {
        public static String CIRCLE_ID = "circuitId";
    }
}
