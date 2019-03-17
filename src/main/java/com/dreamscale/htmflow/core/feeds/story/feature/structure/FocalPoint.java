package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class FocalPoint implements FlowFeature {


    private String name;

    private final Map<String, LocationInPlace> locationMap;
    private final Map<String, Edge> edgeMap;

    private LocationInPlace currentLocation;
    private int locationIndex;

    private static final String ENTRANCE_OF_PLACE = "[entrance]";
    private static final String EXIT_OF_PLACE = "[exit]";



    public FocalPoint(String name, String initialLocationPath) {
        this.name = name;
        this.locationMap = new HashMap<>();
        this.edgeMap = new HashMap<>();

        this.currentLocation = new LocationInPlace(this, initialLocationPath, 0);
        this.locationIndex = 1;
    }

    public String getName() {
        return name;
    }



    public LocationInPlace goToLocation(String locationPath, Duration timeInLocation) {
        LocationInPlace fromLocation = currentLocation;
        LocationInPlace toLocation = findOrCreateLocation(locationPath);

        fromLocation.visit();
        toLocation.visit();

        toLocation.spendTime(timeInLocation);

        Edge edge = findOrCreateEdge(fromLocation, toLocation);
        edge.visit();

        currentLocation = toLocation;
        return toLocation;
    }

    public LocationInPlace getCurrentLocation() {
        return currentLocation;
    }

    public void modifyCurrentLocation(int modificationCount) {
        currentLocation.modify(modificationCount);
    }


    public LocationInPlace exit() {
        return goToLocation(EXIT_OF_PLACE, Duration.ofSeconds(0));
    }

    public LocationInPlace enter() {
        return goToLocation(ENTRANCE_OF_PLACE, Duration.ofSeconds(0));
    }

    private LocationInPlace findOrCreateLocation(String locationPath) {
        LocationInPlace location = locationMap.get(locationPath);
        if (location == null) {
            location = new LocationInPlace(this,locationPath, locationIndex++);
            locationMap.put(locationPath, location);
        }
        return location;
    }

    private Edge findOrCreateEdge(LocationInPlace locationA, LocationInPlace locationB) {
       String edgeKey = createKey(locationA, locationB);

       Edge edge = edgeMap.get(edgeKey);

       if (edge == null) {
           edge = new Edge(locationA, locationB);
           edgeMap.put(edgeKey, edge);
       }

        return edge;
    }

    private String createKey(LocationInPlace locationA, LocationInPlace locationB) {
        String pathA = locationA.getLocationPath();
        String pathB = locationB.getLocationPath();

        if (pathA.compareTo(pathB) > 0) {
            pathA = locationB.getLocationPath();
            pathB = locationA.getLocationPath();
        }

        return pathA + pathB;
    }




    private class Edge {

        private final LocationInPlace locationA;
        private final LocationInPlace locationB;
        private int visitCounter;

        Edge(LocationInPlace locationA, LocationInPlace locationB) {
            this.locationA = locationA;
            this.locationB = locationB;
            this.visitCounter = 0;
        }

        void visit() {
            this.visitCounter++;
        }
    }

}
