package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
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
        Ring firstRing = new Ring(1);
        rings.add(firstRing);
        activeRing = firstRing;
    }

    public void placeCenter(LocationInBox centerOfFocus) {
        if (centerOfFocus != null) {
            this.center = new RingLocation(null, centerOfFocus);
        }
    }

    public void placeEntrance(LocationInBox entrance) {
        if (entrance != null) {
            this.entrance = new RingLocation(null, entrance);
        }
    }

    public void placeExit(LocationInBox exit) {
        if (exit != null) {
            this.exit = new RingLocation(null, exit);
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
        if (center != null && center.location == location) {
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
                ringLink = ring.getLinkForTraversal(traversal);
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

    public List<Traversal> getAllTraversals() {
        List<Traversal> allTraversals = new ArrayList<>();
        for (Ring ring: rings) {
            allTraversals.addAll(ring.getRawTraversals());
        }
        return allTraversals;
    }

    public void setRelativeSequence(int relativeSequence) {
        this.relativeSequence = relativeSequence;
    }


    @Getter
    public static class Ring extends FlowFeature {

        private final int ringNumber;
        List<LocationInBox> locationsInsideRing = new ArrayList<>();
        List<RingLocation> ringLocations = new ArrayList<>();

        List<Link> linksToInnerRing = new ArrayList<>();
        List<Link> linksWithinRing = new ArrayList<>();

        RadialClock radialClock;

        public Ring(int ringNumber) {
            this.ringNumber = ringNumber;
        }

        public RingLocation addElement(LocationInBox location) {
            RingLocation ringLocation = new RingLocation(this, location);

            locationsInsideRing.add(location);
            ringLocations.add(ringLocation);

            return ringLocation;
        }

        RingLocation getRingLocationForLocation(LocationInBox location) {
            int index = locationsInsideRing.indexOf(location);
            return ringLocations.get(index);
        }

        public void addLinkToInnerRing(Link link) {
            this.linksToInnerRing.add(link);
        }

        public void addLinkWithinRing(Link link) {
            this.linksWithinRing.add(link);
        }

        public List<LocationInBox> getRawLocationsInsideRing() {
            return locationsInsideRing;
        }

        public List<RingLocation> getRingLocations() {
            return ringLocations;
        }

        public boolean contains(LocationInBox location) {
            return locationsInsideRing.contains(location);
        }

        void finish() {
            this.radialClock = new RadialClock(ringLocations.size());

            RingLocation [] radialSlots = new RingLocation[ringLocations.size()];

            for (RingLocation item : ringLocations) {
                List<Link> preferredLocationLinks = getPreferredLocations(item);

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

        private List<Link> getPreferredLocations(RingLocation item) {
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

        public Link getLinkForTraversal(Traversal traversal) {
            for (Link link : linksToInnerRing) {
                if (link.contains(traversal)) {
                    return link;
                }
            }
            return null;
        }

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
    }

    public static class RingLocation extends FlowFeature {

        private final LocationInBox location;
        private final Ring parentRing;
        private int slot = 0;
        private double angle = 0;

        public RingLocation(Ring ring, LocationInBox location) {
            this.parentRing = ring;
            this.location = location;
        }

        public String getRingPath() {
            if (parentRing != null) {
                return parentRing.getRelativePath();
            } else {
                return "";
            }
        }

        public int getSlot() {
            return slot;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }

        public double getAngle() {
            return angle;
        }

        public void setAngle(double angle) {
            this.angle = angle;
        }

        public LocationInBox getLocation() {
            return location;
        }

        public boolean contains(LocationInBox location) {
            return this.location == location;
        }
    }

    @Getter
    public static class Link extends FlowFeature {

        private final Traversal traversal;
        private final RingLocation from;
        private final RingLocation to;

        public Link(RingLocation from, RingLocation to, Traversal traversal) {
            this.from = from;
            this.to = to;
            this.traversal = traversal;
        }

        public boolean contains(Traversal traversal) {
            return this.traversal == traversal;
        }

        public boolean contains(RingLocation item) {
            return from == item || to == item;
        }

    }

}
