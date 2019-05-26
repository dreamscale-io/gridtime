package com.dreamscale.htmflow.core.feeds.story.mapper;

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Map;
import java.util.UUID;

public class TileUri {


    public static String createTorchieFeedUri(UUID torchieId) {
        return "/torchie/"+torchieId;
    }

    public static String createCircleFeedUri(UUID circleId) {
        return "/circle/"+circleId;
    }

    public static SourceCoordinates extractCoordinatesFromUri(String tileUri) {
        PathMatcher pathMatcher = new AntPathMatcher();

        Map<String, String> variables = pathMatcher.extractUriTemplateVariables("/torchie/{id}/zoom/{level}/tile/{dreamtime}", tileUri);
        System.out.println(variables);

        UUID torchieId = UUID.fromString(variables.get("id"));
        ZoomLevel zoomLevel = ZoomLevel.valueOf(variables.get("level"));
        GeometryClock.Coords clockCoords = GeometryClock.Coords.fromDreamTime(variables.get("dreamtime"));

        return new SourceCoordinates(torchieId, zoomLevel, clockCoords);
    }

    public static String createTileUri(String feedUri, ZoomLevel zoomLevel, GeometryClock.Coords tileCoordinates) {
        return feedUri + "/zoom/"+zoomLevel.name()+"/tile/"+tileCoordinates.formatDreamTime();
    }

    @Getter
    @AllArgsConstructor
    public static class SourceCoordinates {
        UUID torchieId;
        ZoomLevel zoomLevel;
        GeometryClock.Coords tileCoordinates;
    }
}
