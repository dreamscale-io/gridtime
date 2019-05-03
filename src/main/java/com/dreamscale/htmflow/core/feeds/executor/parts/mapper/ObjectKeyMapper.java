package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;

import java.util.UUID;


public class ObjectKeyMapper {

    public static String createBoxKey(String boxName) {
        return "[Box]:"+ standardize(boxName);
    }

    public static String createLocationKey(String locationPath) {
        return "[Location]:"+ standardizePath(locationPath);
    }

    public static String createBoxLocationKey(String boxPath, String locationPath) {
        return "[Location]:"+ standardizePath(locationPath) +"|[Box]:"+boxPath;
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



    public static String standardizePath(String locationPath) {
        //TODO handle variations of slashes and root directory mappings
        return locationPath;
    }

    public static String standardize(String nameOfThing) {
        return nameOfThing.toLowerCase();
    }


}
