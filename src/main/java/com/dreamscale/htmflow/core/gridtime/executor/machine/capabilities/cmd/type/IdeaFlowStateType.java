package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type;

import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.FeatureDetails;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public enum IdeaFlowStateType implements FeatureType {

    WTF_STATE("/wtf", "/wtf", null),
    LEARNING_STATE("/learn", "/learn", null),
    PROGRESS_STATE("/progress", "/progress", null);

    private static final String CLASS_TYPE = "@flow";

    private static final Set<String> TEMPLATE_VARIABLES = DefaultCollections.emptySet();

    private final Class<? extends FeatureDetails> serializationClass;
    private final UriTemplate uriTemplate;
    private final String uri;
    private final String typeUri;

    IdeaFlowStateType(String typeUri, String uriTemplatePath, Class<? extends FeatureDetails> serializationClass) {
        this.typeUri = CLASS_TYPE + typeUri;
        this.uri = CLASS_TYPE + uriTemplatePath;
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
            return uri;
        }
    }

    public String getUri() {
        return uri;
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
