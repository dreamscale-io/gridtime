package com.dreamscale.gridtime.core.machine.memory.type;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeatureDetails;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeelsRatingDetails;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeelsReference;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public enum FeelsType implements FeatureType {

    FEELS_GOOD("/good", "/good/{rating}", FeelsRatingDetails.class),
    FEELS_PAIN("/pain", "/pain/{rating}", FeelsRatingDetails.class);

    private static final String CLASS_TYPE = "@feel";

    private static final LinkedHashSet<String> TEMPLATE_VARIABLES = DefaultCollections.toSet(TemplateVariable.RATING);

    private final Class<? extends FeatureDetails> serializationClass;
    private final UriTemplate uriTemplate;
    private final String typeUri;

    FeelsType(String typeUri, String uriTemplatePath, Class<? extends FeatureDetails> serializationClass) {
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
        return uriTemplate.expand(templateVariables).toString();
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
        return new FeelsReference(this, searchKey, (FeelsRatingDetails) details);
    }

    @Override
    public String toDisplayString() {
        return typeUri;
    }

    public static class TemplateVariable {
        public static String RATING = "rating";
    }
}
