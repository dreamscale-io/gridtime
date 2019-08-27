package com.dreamscale.gridtime.core.machine.memory.type;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.details.ExecutionEvent;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeatureDetails;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public enum ExecutionEventType implements FeatureType {

    TEST("/test", "/test/{executionId}", ExecutionEvent.class),
    APP("/app", "/app/{executionId}", ExecutionEvent.class);

    private static final String CLASS_TYPE = "@exec";

    private static final LinkedHashSet<String> TEMPLATE_VARIABLES = DefaultCollections.set();

    private final Class<? extends FeatureDetails> serializationClass;
    private final UriTemplate uriTemplate;
    private final String typeUri;

    ExecutionEventType(String typeUri, String uriTemplatePath, Class<? extends FeatureDetails> serializationClass) {
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
    public Class<? extends FeatureDetails> getSerializationClass() {
        return serializationClass;
    }

    @Override
    public String toDisplayString() {
        return typeUri;
    }

    public static class TemplateVariable {
        public static String EXECUTION_ID = "executionId";
    }

}
