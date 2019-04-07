package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.RadialClock;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class RadialStructure {
    private RingLocation center;
    private RingLocation entrance;
    private RingLocation exit;

    private List<Ring> rings = new ArrayList<>();;
    private Ring activeRing;

    private List<Link> linksFromEntrance = new ArrayList<>();
    private List<Link> linksToExit = new ArrayList<>();

    public RadialStructure() {
        Ring firstRing = new Ring(1);
        rings.add(firstRing);
        activeRing = firstRing;
    }

    public void placeCenter(LocationInBox centerOfFocus) {
        this.center = new RingLocation(null, centerOfFocus);
    }

    public void placeEntrance(LocationInBox entrance) {
        this.entrance = new RingLocation(null, entrance);
    }

    public void placeExit(LocationInBox exit) {
        this.exit = new RingLocation(null, exit);
    }

    public void addLinkFromEntrance(LocationInBox connectToLocation, Traversal traversal, double focusWeight, double velocity) {
        if (entrance != null) {
            RingLocation connectToRingLocation = findRingLocation(connectToLocation);

            Link link = new Link(entrance, connectToRingLocation, traversal, focusWeight, velocity);
            linksFromEntrance.add(link);
        }
    }

    public void addLinkToExit(LocationInBox connectFromLocation, Traversal traversal, double focusWeight, double velocity) {
        if (exit != null) {
            RingLocation connectFromRingLocation = findRingLocation(connectFromLocation);


            Link link = new Link(connectFromRingLocation, exit, traversal, focusWeight, velocity);
            linksToExit.add(link);
        }
    }

    public void addLocationToFirstRing(LocationInBox connectToLocation, Traversal traversal, double focusWeight, double velocity) {
        Ring firstRing = rings.get(0);

        RingLocation connectToRingLocation = firstRing.addElement(connectToLocation);

        Link link = new Link(center, connectToRingLocation, traversal, focusWeight, velocity);
        firstRing.addLinkToInnerRing(link);
    }


    public List<LocationInBox> getLocationsInFirstRing() {
        Ring firstRing = rings.get(0);
        return firstRing.getRawLocationsInsideRing();
    }

    public void addExtraLinkWithinFirstRing(LocationInBox locationA, LocationInBox locationB, Traversal traversal, double focusWeight, double velocity) {

        Ring firstRing = rings.get(0);
        RingLocation ringLocationA = firstRing.getRingLocationForLocation(locationA);
        RingLocation ringLocationB = firstRing.getRingLocationForLocation(locationB);

        Link link = new Link(ringLocationA, ringLocationB, traversal, focusWeight, velocity);

        firstRing.addLinkWithinRing(link);
    }

    public void createNextRing() {
        activeRing = new Ring(rings.size() + 1);
        rings.add(activeRing);
    }

    public void addLocationToHighestRing(LocationInBox locationToLinkTo, LocationInBox locationToAdd, Traversal traversal, double focusWeight, double velocity) {
        Ring highestRing = rings.get(rings.size() - 1);

        RingLocation newRingLocation = highestRing.addElement(locationToAdd);
        RingLocation ringLocationToConnectTo = findRingLocation(locationToLinkTo);

        Link link = new Link(ringLocationToConnectTo, newRingLocation, traversal, focusWeight, velocity);
        highestRing.addLinkToInnerRing(link);
    }

    public void addExtraLinkWithinHighestRing(LocationInBox locationA, LocationInBox locationB, Traversal traversal, double focusWeight, double velocity) {

        Ring highestRing = rings.get(rings.size() - 1);

        RingLocation ringLocationA = highestRing.getRingLocationForLocation(locationA);
        RingLocation ringLocationB = highestRing.getRingLocationForLocation(locationB);

        Link link = new Link(ringLocationA, ringLocationB, traversal, focusWeight, velocity);

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

    private RingLocation findRingLocation(LocationInBox connectToLocation) {
        RingLocation ringLocation = null;

        for (Ring ring: rings) {
            if (ring.contains(connectToLocation)) {
                ringLocation = ring.getRingLocationForLocation(connectToLocation);
                break;
            }
        }

        return ringLocation;
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
    }

    @Getter
    public static class Link extends FlowFeature {

        private final Traversal traversal;
        private final RingLocation from;
        private final RingLocation to;
        private final double focusWeight;
        private final double velocity;

        public Link(RingLocation from, RingLocation to, Traversal traversal, double focusWeight, double velocity) {
            this.from = from;
            this.to = to;
            this.traversal = traversal;
            this.focusWeight = focusWeight;
            this.velocity = velocity;
        }

        public boolean contains(RingLocation item) {
            return from == item || to == item;
        }

    }

}
