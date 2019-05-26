package com.dreamscale.htmflow.core.feeds.story.mapper;


import com.dreamscale.htmflow.core.feeds.pool.FeatureType;
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SearchKeyMapper {

    public static String createBoxKey(String boxName) {
        return "[Box]:"+ standardize(boxName);
    }

    public static String createLocationSearchKey(String locationPath) {
        return "[Location]:"+ standardizePath(locationPath);
    }

    public static String createLocationSearchKey(String boxName, String locationPath) {
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(FeatureType.TemplateVariable.BOX_NAME, boxName);
        variableMap.put(FeatureType.TemplateVariable.LOCATION_NAME, standardizePath(locationPath));

        return FeatureType.LOCATION_IN_BOX.expandUri(variableMap);
    }

    public static String createBoxSearchKey(String boxName) {
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(FeatureType.TemplateVariable.BOX_NAME, boxName);

        return FeatureType.BOX.expandUri(variableMap);
    }

    public static String createBridgeSearchKey(String fromBox, String fromLocationPath, String toBox, String toLocationPath) {
        String fromLocation = standardizePath(fromLocationPath);
        String toLocation = standardizePath(toLocationPath);

        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(FeatureType.TemplateVariable.BOX_NAME_A, getFirstSortedBox(fromBox, fromLocation, toBox, toLocation));
        variableMap.put(FeatureType.TemplateVariable.LOCATION_NAME_A, getFirstSortedLocation(fromBox, fromLocation, toBox, toLocation));
        variableMap.put(FeatureType.TemplateVariable.BOX_NAME_B, getSecondSortedBox(fromBox, fromLocation, toBox, toLocation));
        variableMap.put(FeatureType.TemplateVariable.LOCATION_NAME_B, getSecondSortedLocation(fromBox, fromLocation, toBox, toLocation));

        return FeatureType.BRIDGE_BETWEEN_BOXES.expandUri(variableMap);
    }

    public static String createLocationTraversalSearchKey(String boxName, String fromLocationPath, String toLocationPath) {
        String fromLocation = standardizePath(fromLocationPath);
        String toLocation = standardizePath(toLocationPath);

        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(FeatureType.TemplateVariable.BOX_NAME, boxName);
        variableMap.put(FeatureType.TemplateVariable.LOCATION_NAME_A, getFirstSorted(fromLocation, toLocation));
        variableMap.put(FeatureType.TemplateVariable.LOCATION_NAME_B, getSecondSorted(fromLocation, toLocation));

        return FeatureType.TRAVERSAL_IN_BOX.expandUri(variableMap);
    }

    public static String createContextSearchKey(StructureLevel structureLevel, UUID referenceId) {
        FeatureType contextType = getContextType(structureLevel);

        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(FeatureType.TemplateVariable.ID, referenceId.toString());

        return contextType.expandUri(variableMap);
    }

    private static FeatureType getContextType(StructureLevel structureLevel) {
        switch (structureLevel) {
            case PROJECT:
                return FeatureType.PROJECT_CONTEXT;
            case TASK:
                return FeatureType.TASK_CONTEXT;
            case INTENTION:
                return FeatureType.INTENTION_CONTEXT;
        }
        return null;
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


    public static String getFirstSorted(String fromKey, String toKey) {
        String locationA = fromKey;
        String locationB = toKey;

        if (fromKey.compareTo(toKey) > 0) {
            locationA = toKey;
            locationB = fromKey;
        }

        return locationA;
    }

    public static String getSecondSorted(String fromKey, String toKey) {
        String locationA = fromKey;
        String locationB = toKey;

        if (fromKey.compareTo(toKey) > 0) {
            locationA = toKey;
            locationB = fromKey;
        }

        return locationB;
    }

    public static String getFirstSortedBox(String fromBox, String fromLocation, String toBox, String toLocation) {
        return getFirstSorted(fromBox, toBox);
    }

    public static String getFirstSortedLocation(String fromBox, String fromLocation, String toBox, String toLocation) {
        String firstBox = getFirstSorted(fromBox, toBox);
        if (firstBox.equals(fromBox)) {
            return fromLocation;
        } else {
            return toLocation;
        }
    }

    public static String getSecondSortedBox(String fromBox, String fromLocation, String toBox, String toLocation) {
        return getSecondSorted(fromBox, toBox);
    }

    public static String getSecondSortedLocation(String fromBox, String fromLocation, String toBox, String toLocation) {
        String secondBox = getSecondSorted(fromBox, toBox);
        if (secondBox.equals(fromBox)) {
            return fromLocation;
        } else {
            return toLocation;
        }
    }



}
