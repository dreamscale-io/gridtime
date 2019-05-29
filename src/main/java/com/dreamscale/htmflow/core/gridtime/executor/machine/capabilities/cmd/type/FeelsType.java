package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type;

import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.FeatureDetails;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public enum FeelsType implements FeatureType {

    FEELS_GOOD("/good", "/good/{rating}", null),
    FEELS_PAIN("/pain", "/pain/{rating}", null);

    private static final String CLASS_TYPE = "@feel";

    private static final LinkedHashSet<String> TEMPLATE_VARIABLES = DefaultCollections.toSet(TemplateVariable.RATING);

    private final Class<? extends FeatureDetails> serializationClass;
    private final UriTemplate uriTemplate;
    private final String uri;
    private final String typeUri;

    FeelsType(String typeUri, String uriTemplatePath, Class<? extends FeatureDetails> serializationClass) {
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

    public static class TemplateVariable {
        public static String RATING = "rating";
    }
}
