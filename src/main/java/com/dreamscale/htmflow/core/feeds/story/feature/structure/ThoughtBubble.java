package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
public class ThoughtBubble extends FlowFeature {
    private RingLocation center;
    private RingLocation entrance;
    private RingLocation exit;

    private List<Ring> rings = new ArrayList<>();;
    private Ring activeRing;

    private List<Link> linksFromEntrance = new ArrayList<>();
    private List<Link> linksToExit = new ArrayList<>();
    private int relativeSequence;

    public ThoughtBubble() {
        super(FlowObjectType.BUBBLE);
        Ring firstRing = new Ring(1);
        rings.add(firstRing);
        activeRing = firstRing;
    }

    public void placeCenter(LocationInBox centerOfFocus) {
        if (centerOfFocus != null) {
            this.center = new RingLocation(centerOfFocus);
        }
    }

    public void placeEntrance(LocationInBox entrance) {
        if (entrance != null) {
            this.entrance = new RingLocation(entrance);
        }
    }

    public void placeExit(LocationInBox exit) {
        if (exit != null) {
            this.exit = new RingLocation(exit);
        }
    }

    public void addLinkFromEntrance(LocationInBox connectToLocation, Traversal traversal) {
        if (entrance != null) {
            RingLocation connectToRingLocation = findRingLocation(connectToLocation);

            Link link = new Link(entrance, connectToRingLocation, traversal);
            linksFromEntrance.add(link);
        }
    }

    public void addLinkToExit(LocationInBox connectFromLocation, Traversal traversal) {
        if (exit != null) {
            RingLocation connectFromRingLocation = findRingLocation(connectFromLocation);


            Link link = new Link(connectFromRingLocation, exit, traversal);
            linksToExit.add(link);
        }
    }

    public void addLocationToFirstRing(LocationInBox connectToLocation, Traversal traversal) {
        Ring firstRing = rings.get(0);

        RingLocation connectToRingLocation = firstRing.addElement(connectToLocation);

        Link link = new Link(center, connectToRingLocation, traversal);
        firstRing.addLinkToInnerRing(link);
    }

    @JsonIgnore
    public List<LocationInBox> getLocationsInFirstRing() {
        Ring firstRing = rings.get(0);
        return firstRing.getRawLocationsInsideRing();
    }

    public void addExtraLinkWithinFirstRing(LocationInBox locationA, LocationInBox locationB, Traversal traversal) {

        Ring firstRing = rings.get(0);
        RingLocation ringLocationA = firstRing.getRingLocationForLocation(locationA);
        RingLocation ringLocationB = firstRing.getRingLocationForLocation(locationB);

        Link link = new Link(ringLocationA, ringLocationB, traversal);

        firstRing.addLinkWithinRing(link);
    }

    public void createNextRing() {
        activeRing = new Ring(rings.size() + 1);
        rings.add(activeRing);
    }

    public void addLocationToHighestRing(LocationInBox locationToLinkTo, LocationInBox locationToAdd, Traversal traversal) {
        Ring highestRing = rings.get(rings.size() - 1);

        RingLocation newRingLocation = highestRing.addElement(locationToAdd);
        RingLocation ringLocationToConnectTo = findRingLocation(locationToLinkTo);

        Link link = new Link(ringLocationToConnectTo, newRingLocation, traversal);
        highestRing.addLinkToInnerRing(link);
    }

    public void addExtraLinkWithinHighestRing(LocationInBox locationA, LocationInBox locationB, Traversal traversal) {

        Ring highestRing = rings.get(rings.size() - 1);

        RingLocation ringLocationA = highestRing.getRingLocationForLocation(locationA);
        RingLocation ringLocationB = highestRing.getRingLocationForLocation(locationB);

        Link link = new Link(ringLocationA, ringLocationB, traversal);

        highestRing.addLinkWithinRing(link);
    }

    public boolean contains(LocationInBox location) {
        boolean locationFound = false;
        if ((center != null && center.getLocation() == location) ||
            (entrance != null && entrance.getLocation() == location) ||
            (exit != null && exit.getLocation() == location)) {
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

    public void finish() {
        for (Ring ring: rings) {
            ring.finish();
        }
    }

    public RingLocation findRingLocation(LocationInBox connectToLocation) {
        RingLocation ringLocation = null;

        for (Ring ring: rings) {
            if (ring.contains(connectToLocation)) {
                ringLocation = ring.getRingLocationForLocation(connectToLocation);
                break;
            }
        }

        if (ringLocation == null) {
            if (center.contains(connectToLocation)) {
                ringLocation = center;
            }
        }

        if (ringLocation == null) {
            if (entrance.contains(connectToLocation)) {
                ringLocation = entrance;
            }
        }

        if (ringLocation == null) {
            if (exit.contains(connectToLocation)) {
                ringLocation = exit;
            }
        }

        return ringLocation;
    }

    public Link findRingLink(Traversal traversal) {
        Link ringLink = null;

        for (Ring ring: rings) {
            if (ring.containsTraversal(traversal)) {
                ringLink = ring.findLinkForTraversal(traversal);
                break;
            }
        }

        if (ringLink == null) {
            for (Link link: linksFromEntrance) {
                if (link.contains(traversal)) {
                    ringLink = link;
                    break;
                }
            }
        }

        if (ringLink == null) {
            for (Link link: linksToExit) {
                if (link.contains(traversal)) {
                    ringLink = link;
                    break;
                }
            }
        }

        return ringLink;
    }

    @JsonIgnore
    public List<LocationInBox> getAllLocations() {
        List<LocationInBox> allLocations = new ArrayList<>();
        for (Ring ring: rings) {
            allLocations.addAll(ring.getRawLocationsInsideRing());
        }
        if (center != null) allLocations.add(center.getLocation());
        if (entrance != null) allLocations.add(entrance.getLocation());
        if (exit != null) allLocations.add(exit.getLocation());

        return allLocations;
    }

    @JsonIgnore
    public List<Traversal> getAllTraversals() {
        List<Traversal> allTraversals = new ArrayList<>();
        for (Ring ring: rings) {
            allTraversals.addAll(ring.getRawTraversals());
        }
        for (Link link : linksFromEntrance) {
            allTraversals.add(link.getTraversal());
        }
        for (Link link : linksToExit) {
            allTraversals.add(link.getTraversal());
        }

        return allTraversals;
    }

    public void setRelativeSequence(int relativeSequence) {
        this.relativeSequence = relativeSequence;
    }


    @Getter
    @Setter
    @ToString
    public static class Ring extends FlowFeature {

        private int ringNumber;
        List<RingLocation> ringLocations = new ArrayList<>();

        List<Link> linksToInnerRing = new ArrayList<>();
        List<Link> linksWithinRing = new ArrayList<>();

        public Ring(int ringNumber) {
            this();
            this.ringNumber = ringNumber;
        }

        public Ring() {
            super(FlowObjectType.BUBBLE_RING);
        }
        public RingLocation addElement(LocationInBox location) {
            RingLocation ringLocation = new RingLocation(location);

            ringLocations.add(ringLocation);

            return ringLocation;
        }


        public void addLinkToInnerRing(Link link) {
            this.linksToInnerRing.add(link);
        }

        public void addLinkWithinRing(Link link) {
            this.linksWithinRing.add(link);
        }

        @JsonIgnore
        public List<LocationInBox> getRawLocationsInsideRing() {
            List<LocationInBox> locations = new ArrayList<>();
            for (RingLocation ringLocation : ringLocations) {
                locations.add(ringLocation.getLocation());
            }

            return locations;
        }

        @JsonIgnore
        RingLocation getRingLocationForLocation(LocationInBox location) {
            RingLocation locationFound = null;

            for (RingLocation ringLocation : ringLocations) {
                if (ringLocation.getLocation() == location) {
                    locationFound = ringLocation;
                    break;
                }
            }

            return locationFound;
        }

        @JsonIgnore
        public List<Traversal> getRawTraversals() {
            List<Traversal> traversals = new ArrayList<>();
            for (Link link : linksToInnerRing) {
                traversals.add(link.getTraversal());
            }
            for (Link link : linksWithinRing) {
                traversals.add(link.getTraversal());
            }
            return traversals;
        }

        public boolean contains(LocationInBox location) {
            return getRingLocationForLocation(location) != null;
        }

        void finish() {
            RadialClock radialClock = new RadialClock(ringLocations.size());

            RingLocation [] radialSlots = new RingLocation[ringLocations.size()];

            for (RingLocation item : ringLocations) {
                List<Link> preferredLocationLinks = determinePreferredLocations(item);

                List<Double> preferredAngles = new ArrayList<>();

                for (Link link : preferredLocationLinks) {
                    RingLocation innerRingLocation = link.getTo();
                    preferredAngles.add(innerRingLocation.getAngle());
                }

                placeItemInSlot(radialClock, radialSlots, item, preferredAngles);
            }

            ringLocations = Arrays.asList(radialSlots);

        }

        private void placeItemInSlot(RadialClock radialClock, RingLocation[] radialSlots,
                                     RingLocation item, List<Double> preferredAngles) {

            for (Double preferredAngle :preferredAngles) {
                int slotNumber = radialClock.getNearestSlot(preferredAngle);

                if (radialSlots[slotNumber] == null) {
                    radialSlots[slotNumber] = item;
                    configureItemInSlot(radialClock, item, slotNumber);
                    return;
                }

                if (slotNumber + 1 < radialSlots.length - 1 && radialSlots[slotNumber + 1] == null) {
                    radialSlots[slotNumber + 1] = item;
                    configureItemInSlot(radialClock, item, slotNumber + 1);
                    return;
                }

                if (slotNumber - 1 > 0 && radialSlots[slotNumber - 1] == null) {
                    radialSlots[slotNumber - 1] = item;
                    configureItemInSlot(radialClock, item, slotNumber - 1);
                    return;
                }
            }

            //fallback to first available, if none of our preferred worked out
            for (int slotNumber = 0; slotNumber < radialSlots.length ; slotNumber++) {
                if (radialSlots[slotNumber] == null) {
                    radialSlots[slotNumber] = item;
                    configureItemInSlot(radialClock, item, slotNumber);
                    break;
                }
            }

        }

        private void configureItemInSlot(RadialClock radialClock, RingLocation item, int slotNumber) {
            item.setSlot(slotNumber);
            item.setAngle(radialClock.getAngleOfSlot(slotNumber));
        }

        private List<Link> determinePreferredLocations(RingLocation item) {
            List<Link> linksFromItem = new ArrayList<>();
            for (Link link : linksToInnerRing) {
                if (link.contains(item)) {
                    linksFromItem.add(link);
                }
            }
            return linksFromItem;
        }

        public boolean containsTraversal(Traversal traversal) {
            for (Link link : linksToInnerRing) {
                if (link.contains(traversal)) {
                    return true;
                }
            }

            return false;
        }

        public Link findLinkForTraversal(Traversal traversal) {
            for (Link link : linksToInnerRing) {
                if (link.contains(traversal)) {
                    return link;
                }
            }
            return null;
        }


    }

    @Getter
    @Setter
    @ToString
    public static class RingLocation extends FlowFeature {

        private LocationInBox location;
        private int slot = 0;
        private double angle = 0;

        public RingLocation(LocationInBox location) {
            this();
            this.location = location;
        }

        public RingLocation() {
            super(FlowObjectType.BUBBLE_RING_LOCATION);
        }

        public boolean contains(LocationInBox location) {
            return this.location == location;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Link extends FlowFeature {

        private Traversal traversal;
        private RingLocation from;
        private RingLocation to;

        public Link(RingLocation from, RingLocation to, Traversal traversal) {
            this();
            this.from = from;
            this.to = to;
            this.traversal = traversal;
        }

        public Link() {
            super(FlowObjectType.BUBBLE_RING_LINK);
        }

        public boolean contains(Traversal traversal) {
            return this.traversal == traversal;
        }

        public boolean contains(RingLocation item) {
            return from == item || to == item;
        }

    }

}
