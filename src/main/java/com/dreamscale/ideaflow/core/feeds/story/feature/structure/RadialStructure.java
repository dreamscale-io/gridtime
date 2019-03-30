package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import java.util.ArrayList;
import java.util.List;

public class RadialStructure {
    private LocationInThought center;
    private List<Ring> rings = new ArrayList<>();;
    private Ring activeRing;

    public RadialStructure() {
        Ring firstRing = new Ring();
        rings.add(firstRing);
        activeRing = firstRing;
    }

    public void placeCenter(LocationInThought centerOfFocus) {
        this.center = centerOfFocus;
    }

    public void addLocationToFirstRing(LocationInThought connectToLocation, int traversalCount, double focusWeight) {
        Ring firstRing = rings.get(0);

        firstRing.addElement(connectToLocation);

        Link link = new Link(center, connectToLocation, traversalCount, focusWeight);
        firstRing.addLinkToInnerRing(link);
    }

    public List<LocationInThought> getLocationsInFirstRing() {
        Ring firstRing = rings.get(0);
        return firstRing.getLocationsInsideRing();
    }

    public void addExtraLinkWithinFirstRing(LocationInThought locationA, LocationInThought locationB, int traversalCount, double focusWeight) {
        Link link = new Link(locationA, locationB, traversalCount, focusWeight);
        Ring firstRing = rings.get(0);
        firstRing.addLinkWithinRing(link);
    }

    public void createNextRing() {
        activeRing = new Ring();
        rings.add(activeRing);
    }

    public void addLocationToHighestRing(LocationInThought locationToLinkTo, LocationInThought locationToAdd, int traversalCount, double focusWeight) {
        Ring highestRing = rings.get(rings.size() - 1);

        highestRing.addElement(locationToAdd);

        Link link = new Link(locationToLinkTo, locationToAdd, traversalCount, focusWeight);
        highestRing.addLinkToInnerRing(link);
    }

    public void addExtraLinkWithinHighestRing(LocationInThought locationA, LocationInThought locationB, int traversalCount, double focusWeight) {
        Link link = new Link(locationA, locationB, traversalCount, focusWeight);

        Ring highestRing = rings.get(rings.size() - 1);

        highestRing.addLinkWithinRing(link);
    }


    public static class Ring {

        List<LocationInThought> locationsInsideRing = new ArrayList<>();
        List<Link> linksToInnerRing = new ArrayList<>();
        List<Link> linksWithinRing = new ArrayList<>();

        public void addElement(LocationInThought location) {
            locationsInsideRing.add(location);
        }

        public void addLinkToInnerRing(Link link) {
            this.linksToInnerRing.add(link);
        }

        public void addLinkWithinRing(Link link) {
            this.linksWithinRing.add(link);
        }

        public List<LocationInThought> getLocationsInsideRing() {
            return locationsInsideRing;
        }
    }

    public static class Link {

        private final LocationInThought from;
        private final LocationInThought to;
        private final int traversalCount;
        private final double focusWeight;

        public Link(LocationInThought from, LocationInThought to, int traversalCount, double focusWeight) {
            this.from = from;
            this.to = to;
            this.traversalCount = traversalCount;
            this.focusWeight = focusWeight;
        }
    }
}
