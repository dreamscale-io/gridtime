package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.*;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.FeatureDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public enum CmdType implements FeatureType {

    PLAY_TRACK("play", "/track/{trackName}", MusicGridResults.class),
    PLAY_BEAT("play", "/beat/{beatNumber}", MusicGridResults.class),
    PLAY_TILE("play", "/tile/{gridtime}", GridTileResults.class),

    GOTO_TILE("goto", "/tile/{gridtime}", GridTileResults.class),
    GOTO_NEXT_TILE("goto", "/tile/active/next", GridTileResults.class),
    GOTO_PREV_TILE("goto", "/tile/active/prev", GridTileResults.class),

    ZOOM_IN("zoom", "/in", GridTileResults.class),
    ZOOM_OUT("zoom", "/out", GridTileResults.class),

    ID_CELL("id", "/row/{rowNumber}/beat/{beatNumber}", IdentityResults.class),

    SHOW_BOXES("show", "/box", NavOptionResults.class),
    SHOW_BRIDGES("show", "/bridge", NavOptionResults.class),
    SHOW_LOCATIONS_IN_BOX("show", "/box/{sha}/location", NavOptionResults.class),
    SHOW_TRAVERSALS_IN_BRIDGE("show", "/bridge/{sha}/traversal", NavOptionResults.class),

    SHOW_BRIDGES_FROM_BOX("show", "/box/{sha}/to/bridge", NavOptionResults.class),
    SHOW_LOCATIONS_FROM_LOCATION("show", "/location/{sha}/to/location", NavOptionResults.class),

    SHOW_WTFS_IN_BOX("show", "/box/{sha}/wtf", NavOptionResults.class),
    SHOW_WTFS_WITH_LOCATION("show", "/location/{sha}/wtf", NavOptionResults.class),

    OPEN_WTF("open", "/wtf/{sha}", OpenCircleResults.class),

    SEE_FROM_BOX("see-from", "/box/{sha}", SeeFromPlaceResults.class),
    SEE_FROM_LOCATION("see-from", "/location/{sha}", SeeFromPlaceResults.class),
    SEE_FROM_BRIDGE("see-from", "/bridge/{sha}", SeeFromPlaceResults.class),
    SEE_FROM_WIRE("see-from", "/wire/{sha}", SeeFromPlaceResults.class),

    ALARM_WHEN_BOX_IS_FULL_OF_WTF("alarm-when", "/threshold/wtf/count/box/{sha}", AlarmIsOnResults.class),
    ALARM_WHEN_BOX_IS_FULL_OF_UNCERTAINTY("alarm-when", "/threshold/learning/count/box/{sha}", AlarmIsOnResults.class);

    private final String cmdName;
    private final Class<?> resultsClazz;
    private final UriTemplate uriTemplateWithType;
    private final String uriTemplateStr;

    private static final String CLASS_TYPE = "@cmd";

    private static final LinkedHashSet<String> TEMPLATE_VARIABLES =
            DefaultCollections.toSet(
                    TemplateVariable.SHA,
                    TemplateVariable.ROW_NUMBER,
                    TemplateVariable.BEAT_NUMBER,
                    TemplateVariable.GRIDTIME);



    CmdType(String cmdName, String uriTemplateStr, Class<?> resultsClazz) {
        this.cmdName = cmdName;
        this.uriTemplateStr = uriTemplateStr;
        this.uriTemplateWithType = new UriTemplate(CLASS_TYPE + uriTemplateStr);
        this.resultsClazz = resultsClazz;
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
        return uriTemplateWithType.expand(templateVariables).toString();
    }

    @Override
    public Class<? extends FeatureDetails> getSerializationClass() {
        return null;
    }

    @Override
    public String toDisplayString() {
        return cmdName + uriTemplateStr;
    }

    public Map<String, String> extractParameters(String cmdStr) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return pathMatcher.extractUriTemplateVariables(cmdName + " " + uriTemplateStr, cmdStr);
    }

    public static class TemplateVariable {
        public static String SHA = "sha";
        public static String ROW_NUMBER = "rowNumber";
        public static String BEAT_NUMBER = "beatNumber";
        public static String GRIDTIME = "gridtime";
    }

}
