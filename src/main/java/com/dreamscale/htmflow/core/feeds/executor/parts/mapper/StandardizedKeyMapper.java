package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.ZoomLevel;

import java.util.UUID;


public class StandardizedKeyMapper {

    public static String createBoxKey(String boxName) {
        return "[Box]:"+ standardize(boxName);
    }

    public static String createLocationKey(String locationPath) {
        return "[Location]:"+ standardizePath(locationPath);
    }

    public static String createLocationTraversalKey(String fromLocationKey, String toLocationKey) {
        String locationA = fromLocationKey;
        String locationB = toLocationKey;

        if (fromLocationKey.compareTo(toLocationKey) > 0) {
            locationA = toLocationKey;
            locationB = fromLocationKey;
        }
        return "[Traversal]:[" + locationA + "]=>[" + locationB + "]";
    }

    public static String createBridgeKey(String fromLocationKey, String toLocationKey) {
        String locationA = fromLocationKey;
        String locationB = toLocationKey;

        if (fromLocationKey.compareTo(toLocationKey) > 0) {
            locationA = toLocationKey;
            locationB = fromLocationKey;
        }
        return "[Bridge]:[" + locationA + "]=>[" + locationB + "]";
    }

    public static String createTorchieFeedUri(UUID torchieId) {
        return "@torchie/"+torchieId;
    }

    public static String createCircleFeedUri(UUID circleId) {
        return "@circle/"+circleId;
    }

    public static String createTileUri(String feedUri, ZoomLevel zoomLevel, OuterGeometryClock.Coords tileCoordinates) {
        return feedUri + "/zoom/"+zoomLevel.name()+"/tile/"+tileCoordinates.formatCoords();
    }

    public static String standardizePath(String locationPath) {
        //TODO handle variations of slashes and root directory mappings
        return locationPath;
    }

    public static String standardize(String nameOfThing) {
        return nameOfThing.toLowerCase();
    }


}
