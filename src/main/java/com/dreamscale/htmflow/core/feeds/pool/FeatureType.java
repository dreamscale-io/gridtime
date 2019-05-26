package com.dreamscale.htmflow.core.feeds.pool;

import com.dreamscale.htmflow.core.feeds.pool.feature.*;
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel;
import lombok.Getter;
import org.springframework.web.util.UriTemplate;

import java.util.Map;

@Getter
public enum FeatureType {

    BOX("@place/box/{boxName}", Box.class),
    LOCATION_IN_BOX("@place/box/{boxName}/location/{locationName}", LocationInBox.class),
    TRAVERSAL_IN_BOX("@place/box/{boxName}/traversal/A/location/{locationNameA}/B/location/{locationNameB}", Traversal.class),
    BRIDGE_BETWEEN_BOXES("@place/bridge/A/box/{boxNameA}/location/{locationNameA}/B/box/{boxNameB}/location/{locationNameB}", Bridge.class),

    PROJECT_CONTEXT("@context/project/{id}",Context.class),
    TASK_CONTEXT("@context/task/{id}",Context.class),
    INTENTION_CONTEXT("@context/intention/{id}",Context.class);


    private final Class<? extends GridFeature> serializationClass;
    private final UriTemplate uriTemplate;


    FeatureType(String uriTemplatePath, Class<? extends GridFeature> serializationClass) {
        this.uriTemplate = new UriTemplate(uriTemplatePath);
        this.serializationClass = serializationClass;
    }

    public String expandUri(Map<String, String> templateVariables) {
        return uriTemplate.expand(templateVariables).toString();
    }

    public static FeatureType getContextType(StructureLevel structureLevel) {
        switch (structureLevel) {
            case PROJECT:
                return PROJECT_CONTEXT;
            case TASK:
                return TASK_CONTEXT;
            case INTENTION:
                return INTENTION_CONTEXT;
        }
        return null;
    }

    public static class TemplateVariable {
        public static String BOX_NAME = "boxName";
        public static String LOCATION_NAME = "locationName";
        public static String BOX_NAME_A = "boxNameA";
        public static String LOCATION_NAME_A = "locationNameA";
        public static String BOX_NAME_B = "boxNameB";
        public static String LOCATION_NAME_B = "locationNameB";

        public static String ID = "id";
    }

}
