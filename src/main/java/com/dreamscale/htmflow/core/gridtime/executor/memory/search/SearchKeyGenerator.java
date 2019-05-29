package com.dreamscale.htmflow.core.gridtime.executor.memory.search;


import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.*;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.StructureLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SearchKeyGenerator {

    public static String createBoxKey(String boxName) {
        return "[Box]:"+ standardize(boxName);
    }

    public static String createLocationSearchKey(String locationPath) {
        return "[Location]:"+ standardizePath(locationPath);
    }

    public static String createFeelsSearchKey(Integer rating) {
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(FeelsType.TemplateVariable.RATING, rating.toString());

        if (rating >= 0) {
            return FeelsType.FEELS_GOOD.expandUri(variableMap);
        } else {
            return FeelsType.FEELS_PAIN.expandUri(variableMap);
        }
    }

    public static String createLocationSearchKey(UUID projectId, String locationPath) {
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(PlaceType.TemplateVariable.PROJECT_ID, projectId.toString());
        variableMap.put(PlaceType.TemplateVariable.LOCATION_NAME, standardizePath(locationPath));

        return PlaceType.LOCATION_IN_BOX.expandUri(variableMap);
    }

    public static String createBoxSearchKey(String boxName) {
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(PlaceType.TemplateVariable.BOX_NAME, boxName);

        return PlaceType.BOX.expandUri(variableMap);
    }

    public static String createBridgeSearchKey(String fromBox, String fromLocationPath, String toBox, String toLocationPath) {
        String fromLocation = standardizePath(fromLocationPath);
        String toLocation = standardizePath(toLocationPath);

        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(PlaceType.TemplateVariable.BOX_NAME_A, getFirstSortedBox(fromBox, fromLocation, toBox, toLocation));
        variableMap.put(PlaceType.TemplateVariable.LOCATION_NAME_A, getFirstSortedLocation(fromBox, fromLocation, toBox, toLocation));
        variableMap.put(PlaceType.TemplateVariable.BOX_NAME_B, getSecondSortedBox(fromBox, fromLocation, toBox, toLocation));
        variableMap.put(PlaceType.TemplateVariable.LOCATION_NAME_B, getSecondSortedLocation(fromBox, fromLocation, toBox, toLocation));

        return PlaceType.TRAVERSAL_WIRE_BRIDGE_BETWEEN_BOXES.expandUri(variableMap);
    }

    public static String createExecutionKey(boolean isTest, Long executionId) {

        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(ExecutionEventType.TemplateVariable.EXECUTION_ID, executionId.toString());

        if (isTest ) {
            return ExecutionEventType.TEST.expandUri(variableMap);
        } else {
            return ExecutionEventType.APP.expandUri(variableMap);
        }
    }

    public static String createTraversalSearchKey(UUID projectId, String fromLocationPath, String toLocationPath) {
        String fromLocation = standardizePath(fromLocationPath);
        String toLocation = standardizePath(toLocationPath);

        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(PlaceType.TemplateVariable.PROJECT_ID, projectId.toString());
        variableMap.put(PlaceType.TemplateVariable.LOCATION_NAME_A, getFirstSorted(fromLocation, toLocation));
        variableMap.put(PlaceType.TemplateVariable.LOCATION_NAME_B, getSecondSorted(fromLocation, toLocation));

        return PlaceType.TRAVERSAL_WIRE.expandUri(variableMap);
    }

    public static String createContextSearchKey(StructureLevel structureLevel, UUID referenceId) {
        WorkContextType contextType = getContextType(structureLevel);

        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(WorkContextType.TemplateVariable.SHA, referenceId.toString());

        return contextType.expandUri(variableMap);
    }

    private static WorkContextType getContextType(StructureLevel structureLevel) {
        switch (structureLevel) {
            case PROJECT:
                return WorkContextType.PROJECT_WORK;
            case TASK:
                return WorkContextType.TASK_WORK;
            case INTENTION:
                return WorkContextType.INTENTION_WORK;
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


    public static String createAuthorsSearchKey(List<Member> authors) {
        String authorsKey = "";

        AuthorsType authorsType = null;

        if (authors.size() <= 1) {
            authorsType = AuthorsType.SOLO;
        } else if (authors.size() == 2) {
            authorsType = AuthorsType.PAIR;
        } else {
            authorsType = AuthorsType.MOB;
        }

        for (Member author : authors) {
            authorsKey += author.getMemberId();
        }

        Map<String, String> variableMap = new HashMap<>();
        variableMap.put(AuthorsType.TemplateVariable.AUTHORS_KEY, authorsKey);

        return authorsType.expandUri(variableMap);
    }


}
