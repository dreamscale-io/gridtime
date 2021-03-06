package com.dreamscale.gridtime.core.machine.memory.type;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.details.*;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.IdeaFlowStateReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public enum PlaceType implements FeatureType {

    BOX("/box", "/project/{projectId}/box/{boxName}", Box.class),
    LOCATION("/location", "/project/{projectId}/location/{locationName}", LocationInBox.class),
    TRAVERSAL_IN_BOX("/traversal", "/project/{projectId}/traversal/box/{boxName}/A/location/{locationNameA}/B/location/{locationNameB}", Traversal.class),
    BRIDGE_BETWEEN_BOXES("/bridge", "/project/{projectId}/bridge/A/box/{boxNameA}/location/{locationNameA}/B/box/{boxNameB}/location/{locationNameB}", Bridge.class);

    private final String typeUri;
    private final Class<? extends FeatureDetails> serializationClass;
    private final UriTemplate uriTemplate;

    private static final String CLASS_TYPE = "@place";

    private static final LinkedHashSet<String> TEMPLATE_VARIABLES =
            DefaultCollections.toSet(
                TemplateVariable.BOX_NAME,
                TemplateVariable.LOCATION_NAME,
                    TemplateVariable.BOX_NAME_A,
                    TemplateVariable.LOCATION_NAME_A,
                    TemplateVariable.BOX_NAME_B,
                    TemplateVariable.LOCATION_NAME_B);


    PlaceType(String typeUri, String uriTemplatePath, Class<? extends FeatureDetails> serializationClass) {
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

    @Override
    public FeatureReference createReference(String searchKey, FeatureDetails details) {
        return new PlaceReference(this, searchKey, details);
    }

    @Override
    public String toDisplayString() {
        return typeUri;
    }


    public static class TemplateVariable {
        public static String PROJECT_ID = "projectId";
        public static String BOX_NAME = "boxName";
        public static String LOCATION_NAME = "locationName";
        public static String BOX_NAME_A = "boxNameA";
        public static String LOCATION_NAME_A = "locationNameA";
        public static String BOX_NAME_B = "boxNameB";
        public static String LOCATION_NAME_B = "locationNameB";
    }

}
