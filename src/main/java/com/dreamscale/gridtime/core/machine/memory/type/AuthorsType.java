package com.dreamscale.gridtime.core.machine.memory.type;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.details.*;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public enum AuthorsType implements FeatureType {

    SOLO("/me", "/me/{authorsKey}", null),
    PAIR("/pair", "/pair/{authorsKey}", null),
    MOB("/mob", "/mob/{authorsKey}", null);

    private final Class<? extends FeatureDetails> serializationClass;
    private final String typeUri;
    private final UriTemplate uriTemplate;

    private static final String CLASS_TYPE = "@author";

    private static final LinkedHashSet<String> TEMPLATE_VARIABLES =
            DefaultCollections.toSet(TemplateVariable.AUTHORS_KEY);



    AuthorsType(String typeUri, String uriTemplatePath, Class<? extends FeatureDetails> serializationClass) {
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
        public static String AUTHORS_KEY = "authorsKey";
    }

}
