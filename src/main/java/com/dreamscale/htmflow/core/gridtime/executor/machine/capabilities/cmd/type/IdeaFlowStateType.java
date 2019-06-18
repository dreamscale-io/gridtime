package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type;

import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.FeatureDetails;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.Set;

public enum IdeaFlowStateType implements FeatureType {

    WTF_STATE("/wtf"),
    LEARNING_STATE("/learn"),
    PROGRESS_STATE("/progress");

    private static final String CLASS_TYPE = "@flow";

    private static final Set<String> TEMPLATE_VARIABLES = DefaultCollections.emptySet();

    private final Class<? extends FeatureDetails> serializationClass;
    private final UriTemplate uriTemplate;
    private final String typeUri;

    IdeaFlowStateType(String typeUri) {
        this.typeUri = CLASS_TYPE + typeUri;
        this.uriTemplate = new UriTemplate(CLASS_TYPE + typeUri);
        this.serializationClass = null;
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

}
