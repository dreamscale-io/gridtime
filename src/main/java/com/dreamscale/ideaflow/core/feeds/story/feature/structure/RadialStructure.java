package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import java.util.ArrayList;
import java.util.List;

public class RadialStructure {
    private LocationInFocus center;
    private LocationInFocus entrance;
    private LocationInFocus exit;

    private List<Ring> rings = new ArrayList<>();;
    private Ring activeRing;

    private List<Link> linksFromEntrance = new ArrayList<>();
    private List<Link> linksToExit = new ArrayList<>();

    public RadialStructure() {
        Ring firstRing = new Ring();
        rings.add(firstRing);
        activeRing = firstRing;
    }

    public void placeCenter(LocationInFocus centerOfFocus) {
        this.center = centerOfFocus;
    }

    public void placeEntrance(LocationInFocus entrance) {
        this.entrance = entrance;
    }

    public void placeExit(LocationInFocus exit) {
        this.exit = exit;
    }

    public void addLinkFromEntrance(LocationInFocus connectToLocation, int traversalCount, double focusWeight, double velocity) {
        if (entrance != null) {
            Link link = new Link(entrance, connectToLocation, traversalCount, focusWeight, velocity);
            linksFromEntrance.add(link);
        }
    }

    public void addLinkToExit(LocationInFocus connectFromLocation, int traversalCount, double focusWeight, double velocity) {
        if (exit != null) {
            Link link = new Link(connectFromLocation, exit, traversalCount, focusWeight, velocity);
            linksToExit.add(link);
        }
    }

    public void addLocationToFirstRing(LocationInFocus connectToLocation, int traversalCount, double focusWeight, double velocity) {
        Ring firstRing = rings.get(0);

        firstRing.addElement(connectToLocation);

        Link link = new Link(center, connectToLocation, traversalCount, focusWeight, velocity);
        firstRing.addLinkToInnerRing(link);
    }


    public List<LocationInFocus> getLocationsInFirstRing() {
        Ring firstRing = rings.get(0);
        return firstRing.getLocationsInsideRing();
    }

    public void addExtraLinkWithinFirstRing(LocationInFocus locationA, LocationInFocus locationB, int traversalCount, double focusWeight, double velocity) {
        Link link = new Link(locationA, locationB, traversalCount, focusWeight, velocity);
        Ring firstRing = rings.get(0);
        firstRing.addLinkWithinRing(link);
    }

    public void createNextRing() {
        activeRing = new Ring();
        rings.add(activeRing);
    }

    public void addLocationToHighestRing(LocationInFocus locationToLinkTo, LocationInFocus locationToAdd, int traversalCount, double focusWeight, double velocity) {
        Ring highestRing = rings.get(rings.size() - 1);

        highestRing.addElement(locationToAdd);

        Link link = new Link(locationToLinkTo, locationToAdd, traversalCount, focusWeight, velocity);
        highestRing.addLinkToInnerRing(link);
    }

    public void addExtraLinkWithinHighestRing(LocationInFocus locationA, LocationInFocus locationB, int traversalCount, double focusWeight, double velocity) {
        Link link = new Link(locationA, locationB, traversalCount, focusWeight, velocity);

        Ring highestRing = rings.get(rings.size() - 1);

        highestRing.addLinkWithinRing(link);
    }

    public boolean contains(LocationInFocus location) {
        boolean locationFound = false;
        if (location == center) {
            locationFound = true;
        } else {
            for (Ring ring: rings) {
                if (ring.contains(location)) {
                    locationFound = true;
                    break;
                }
            }
        }

        return locationFound;
    }


    public static class Ring {

        List<LocationInFocus> locationsInsideRing = new ArrayList<>();
        List<Link> linksToInnerRing = new ArrayList<>();
        List<Link> linksWithinRing = new ArrayList<>();

        public void addElement(LocationInFocus location) {
            locationsInsideRing.add(location);
        }

        public void addLinkToInnerRing(Link link) {
            this.linksToInnerRing.add(link);
        }

        public void addLinkWithinRing(Link link) {
            this.linksWithinRing.add(link);
        }

        public List<LocationInFocus> getLocationsInsideRing() {
            return locationsInsideRing;
        }

        public boolean contains(LocationInFocus location) {
            return locationsInsideRing.contains(location);
        }
    }

    public static class Link {

        private final LocationInFocus from;
        private final LocationInFocus to;
        private final int traversalCount;
        private final double focusWeight;
        private final double velocity;

        public Link(LocationInFocus from, LocationInFocus to, int traversalCount, double focusWeight, double velocity) {
            this.from = from;
            this.to = to;
            this.traversalCount = traversalCount;
            this.focusWeight = focusWeight;
            this.velocity = velocity;
        }
    }
}
