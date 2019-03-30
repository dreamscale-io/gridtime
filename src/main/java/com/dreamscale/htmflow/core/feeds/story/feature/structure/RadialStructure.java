package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import java.util.ArrayList;
import java.util.List;

public class RadialStructure {
    private LocationInPlace center;
    private Ring firstRing = new Ring();
    private List<Ring> outerRings = new ArrayList<>();

    public void placeCenter(LocationInPlace centerOfFocus) {
        this.center = centerOfFocus;
    }

    public void addLocationToFirstRing(LocationInPlace connectToLocation, int traversalCount, double focusWeight) {
        firstRing.addElement(connectToLocation);

        Link link = new Link(center, connectToLocation, traversalCount, focusWeight);
        firstRing.addLinkToInnerRing(link);
    }

    public List<LocationInPlace> getLocationsInFirstRing() {
        return firstRing.getLocationsInRing();
    }

    public void addExtraLinkWithinFirstRing(LocationInPlace locationA, LocationInPlace locationB, int traversalCount, double focusWeight) {
        Link link = new Link(locationA, locationB, traversalCount, focusWeight);
        firstRing.addLinkWithinRing(link);
    }


    public static class Ring {

        List<LocationInPlace> locationsInRing = new ArrayList<>();
        List<Link> linksToInnerRing = new ArrayList<>();
        List<Link> linksWithinRing = new ArrayList<>();
        List<Link> linksToOuterRing = new ArrayList<>();

        public void addElement(LocationInPlace connectToLocation) {
            locationsInRing.add(connectToLocation);
        }

        public void addLinkToInnerRing(Link link) {
            this.linksToInnerRing.add(link);
        }

        public void addLinkWithinRing(Link link) {
            this.linksWithinRing.add(link);
        }

        public List<LocationInPlace> getLocationsInRing() {
            return locationsInRing;
        }
    }

    public static class Link {

        private final LocationInPlace from;
        private final LocationInPlace to;
        private final int traversalCount;
        private final double focusWeight;

        public Link(LocationInPlace from, LocationInPlace to, int traversalCount, double focusWeight) {
            this.from = from;
            this.to = to;
            this.traversalCount = traversalCount;
            this.focusWeight = focusWeight;
        }
    }
}
