package com.dreamscale.htmflow.core.gridtime.executor.memory.tile;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.GridTime;
import com.dreamscale.htmflow.core.gridtime.executor.clock.ZoomLevel;
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

        Map<String, String> variables = pathMatcher.extractUriTemplateVariables("/torchie/{id}/zoom/{level}/tile/{gridtime}", tileUri);
        System.out.println(variables);

        UUID torchieId = UUID.fromString(variables.get("id"));
        ZoomLevel zoomLevel = ZoomLevel.valueOf(variables.get("level"));
        GridTime gridTime = GridTime.fromGridTime(variables.get("gridtime"));

        return new SourceCoordinates(torchieId, zoomLevel, new GeometryClock.Coords(gridTime.toClockTime(), gridTime));
    }

    public static String createTileUri(String feedUri, ZoomLevel zoomLevel, GeometryClock.Coords tileCoordinates) {
        return feedUri + "/zoom/"+zoomLevel.name()+"/tile/"+tileCoordinates.getFormattedGridTime();
    }

    @Getter
    @AllArgsConstructor
    public static class SourceCoordinates {
        UUID torchieId;
        ZoomLevel zoomLevel;
        GeometryClock.Coords tileCoordinates;
    }
}
