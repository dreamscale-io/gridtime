package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.mapper.RadialClock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RadialStructure {
    private RingLocation center;
    private RingLocation entrance;
    private RingLocation exit;

    private List<Ring> rings = new ArrayList<>();;
    private Ring activeRing;

    private List<Link> linksFromEntrance = new ArrayList<>();
    private List<Link> linksToExit = new ArrayList<>();

    public RadialStructure() {
        Ring firstRing = new Ring();
        rings.add(firstRing);
        activeRing = firstRing;
    }

    public void placeCenter(LocationInPlace centerOfFocus) {
        this.center = new RingLocation(centerOfFocus);
    }

    public void placeEntrance(LocationInPlace entrance) {
        this.entrance = new RingLocation(entrance);
    }

    public void placeExit(LocationInPlace exit) {
        this.exit = new RingLocation(exit);
    }

    public void addLinkFromEntrance(LocationInPlace connectToLocation, int traversalCount, double focusWeight, double velocity) {
        if (entrance != null) {
            RingLocation connectToRingLocation = findRingLocation(connectToLocation);

            Link link = new Link(entrance, connectToRingLocation, traversalCount, focusWeight, velocity);
            linksFromEntrance.add(link);
        }
    }

    public void addLinkToExit(LocationInPlace connectFromLocation, int traversalCount, double focusWeight, double velocity) {
        if (exit != null) {
            RingLocation connectFromRingLocation = findRingLocation(connectFromLocation);


            Link link = new Link(connectFromRingLocation, exit, traversalCount, focusWeight, velocity);
            linksToExit.add(link);
        }
    }

    public void addLocationToFirstRing(LocationInPlace connectToLocation, int traversalCount, double focusWeight, double velocity) {
        Ring firstRing = rings.get(0);

        RingLocation connectToRingLocation = firstRing.addElement(connectToLocation);

        Link link = new Link(center, connectToRingLocation, traversalCount, focusWeight, velocity);
        firstRing.addLinkToInnerRing(link);
    }


    public List<LocationInPlace> getLocationsInFirstRing() {
        Ring firstRing = rings.get(0);
        return firstRing.getRawLocationsInsideRing();
    }

    public void addExtraLinkWithinFirstRing(LocationInPlace locationA, LocationInPlace locationB, int traversalCount, double focusWeight, double velocity) {

        Ring firstRing = rings.get(0);
        RingLocation ringLocationA = firstRing.getRingLocationForLocation(locationA);
        RingLocation ringLocationB = firstRing.getRingLocationForLocation(locationB);

        Link link = new Link(ringLocationA, ringLocationB, traversalCount, focusWeight, velocity);

        firstRing.addLinkWithinRing(link);
    }

    public void createNextRing() {
        activeRing = new Ring();
        rings.add(activeRing);
    }

    public void addLocationToHighestRing(LocationInPlace locationToLinkTo, LocationInPlace locationToAdd, int traversalCount, double focusWeight, double velocity) {
        Ring highestRing = rings.get(rings.size() - 1);

        RingLocation newRingLocation = highestRing.addElement(locationToAdd);
        RingLocation ringLocationToConnectTo = findRingLocation(locationToLinkTo);

        Link link = new Link(ringLocationToConnectTo, newRingLocation, traversalCount, focusWeight, velocity);
        highestRing.addLinkToInnerRing(link);
    }

    public void addExtraLinkWithinHighestRing(LocationInPlace locationA, LocationInPlace locationB, int traversalCount, double focusWeight, double velocity) {

        Ring highestRing = rings.get(rings.size() - 1);

        RingLocation ringLocationA = highestRing.getRingLocationForLocation(locationA);
        RingLocation ringLocationB = highestRing.getRingLocationForLocation(locationB);

        Link link = new Link(ringLocationA, ringLocationB, traversalCount, focusWeight, velocity);

        highestRing.addLinkWithinRing(link);
    }

    public boolean contains(LocationInPlace location) {
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

    private RingLocation findRingLocation(LocationInPlace connectToLocation) {
        RingLocation ringLocation = null;

        for (Ring ring: rings) {
            if (ring.contains(connectToLocation)) {
                ringLocation = ring.getRingLocationForLocation(connectToLocation);
                break;
            }
        }

        return ringLocation;
    }

    public static class Ring {


        List<LocationInPlace> locationsInsideRing = new ArrayList<>();
        List<RingLocation> ringLocations = new ArrayList<>();

        List<Link> linksToInnerRing = new ArrayList<>();
        List<Link> linksWithinRing = new ArrayList<>();


        RadialClock radialClock;


        public RingLocation addElement(LocationInPlace location) {
            RingLocation ringLocation = new RingLocation(location);

            locationsInsideRing.add(location);
            ringLocations.add(ringLocation);

            return ringLocation;
        }

        RingLocation getRingLocationForLocation(LocationInPlace location) {
            int index = locationsInsideRing.indexOf(location);
            return ringLocations.get(index);
        }

        public void addLinkToInnerRing(Link link) {
            this.linksToInnerRing.add(link);
        }

        public void addLinkWithinRing(Link link) {
            this.linksWithinRing.add(link);
        }

        public List<LocationInPlace> getRawLocationsInsideRing() {
            return locationsInsideRing;
        }

        public List<RingLocation> getRingLocations() {
            return ringLocations;
        }

        public boolean contains(LocationInPlace location) {
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
    }

    public static class RingLocation {

        private final LocationInPlace location;
        private int slot = 0;
        private double angle = 0;

        public RingLocation(LocationInPlace location) {
            this.location = location;
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
    }

    public static class Link {

        private final RingLocation from;
        private final RingLocation to;
        private final int traversalCount;
        private final double focusWeight;
        private final double velocity;

        public Link(RingLocation from, RingLocation to, int traversalCount, double focusWeight, double velocity) {
            this.from = from;
            this.to = to;
            this.traversalCount = traversalCount;
            this.focusWeight = focusWeight;
            this.velocity = velocity;
        }

        public boolean contains(RingLocation item) {
            return from == item || to == item;
        }

        public RingLocation getFrom() {
            return from;
        }

        public RingLocation getTo() {
            return to;
        }
    }

}
