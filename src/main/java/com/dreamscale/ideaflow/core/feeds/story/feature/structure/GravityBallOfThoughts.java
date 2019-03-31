package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import java.time.Duration;
import java.util.*;

/**
 * Create a 2D structural model of a FocalPoint by first identifying the "focal center",
 * then orienting positions of elements in 2D space as a radial map, relative to focal center
 *
 * For each disconnected network, create a new radial map around the next focal center,
 * to create a stream of "thought bubbles"
 */

public class GravityBallOfThoughts {

    private final Map<String, LocationInFocus> locationMap = new HashMap<>();
    private final Map<String, Link> linkMap = new HashMap<>();
    private final FocalPoint focalPoint;

    private Map<String, ThoughtParticle> thoughtParticleMap = new HashMap<>();
    private LinkedList<ThoughtParticle> thoughtTracer = new LinkedList<>();

    private LocationInFocus currentLocation;
    private int locationIndex = 1;

    private static final int TRACER_LENGTH = 5;

    private static final String ENTRANCE_OF_PLACE = "[entrance]";
    private static final String EXIT_OF_PLACE = "[exit]";

    private LocationInFocus exitLocation;
    private LocationInFocus entranceLocation;


    public GravityBallOfThoughts(FocalPoint focalPoint) {
        this.focalPoint = focalPoint;
    }

    public LocationInFocus gotoLocationInSpace(String locationPath) {

        LocationInFocus fromLocation = currentLocation;
        LocationInFocus toLocation = findOrCreateLocation(locationPath);

        toLocation.visit();
        currentLocation = toLocation;

        addThoughtParticleForTraversal(fromLocation, toLocation);

        return toLocation;
    }

    public LocationInFocus gotoExit() {
        this.exitLocation = gotoLocationInSpace(EXIT_OF_PLACE);
        return this.exitLocation;
    }

    public LocationInFocus gotoEntrance() {
        this.entranceLocation = gotoLocationInSpace(ENTRANCE_OF_PLACE);
        return this.entranceLocation;
    }


    public LocationInFocus getCurrentLocation() {
        return currentLocation;
    }

    public void growHeavyWithFocus(Duration timeInLocation) {
        currentLocation.spendTime(timeInLocation);
        thoughtTracer.get(0).setVelocityOfTransition(timeInLocation);

        DecayingGrowthRate decayingGrowth = new DecayingGrowthRate(timeInLocation);

        //pushing a new thought particle on the tracer, also has a side-effect of decreasing the weight
        //of ALL existing particles by 1 increment of decay, for any particles linked in the active tracer,
        //grow the particle weight

        for (ThoughtParticle existingParticle : thoughtParticleMap.values()) {
            existingParticle.decayWithFocusElsewhere(decayingGrowth);
        }

        for (ThoughtParticle particle : thoughtTracer) {
            particle.growHeavyWithFocus(decayingGrowth, timeInLocation);

            decayingGrowth.decay();
        }
    }

    private void addThoughtParticleForTraversal(LocationInFocus fromLocation, LocationInFocus toLocation) {

        Link edge = findOrCreateEdge(fromLocation, toLocation);
        edge.visit();

        ThoughtParticle particle = findOrCreateParticle(edge);

        pushThoughtParticleOntoTracer(particle);
    }

    private void pushThoughtParticleOntoTracer(ThoughtParticle particle) {
        thoughtTracer.push(particle);

        if (thoughtTracer.size() > TRACER_LENGTH) {
            thoughtTracer.removeLast();
        }

    }

    public List<RadialStructure> extractThoughtBubbles() {

        List<ThoughtParticle> particlesByWeight = getNormalizedParticlesSortedByWeight();

        List<ThoughtParticle> enterExitTransitions = findEnterExitTransitions(particlesByWeight);
        particlesByWeight.removeAll(enterExitTransitions);

        List<RadialStructure> radialStructures = new ArrayList<>();

        //if I've got left over stuff after a loop, these are disconnected networks with alt-centers,
        // so make radial structures for each network

        int lastParticlesRemaining = particlesByWeight.size();

        while (particlesByWeight.size() > 0) {

            RadialStructure radialStructure = createRadialStructureAndRemoveParticlesUsed(particlesByWeight);

            List<ThoughtParticle> particlesUsed = addEnterExitsToStructure(radialStructure, enterExitTransitions);
            enterExitTransitions.removeAll(particlesUsed);
            particlesByWeight.removeAll(particlesUsed);

            radialStructures.add(radialStructure);

            //we should deplete our particles, but just in case, make sure we don't loop forever
            if (lastParticlesRemaining == particlesByWeight.size()) {
                break;
            } else {
                lastParticlesRemaining = particlesByWeight.size();
            }
        }

        return radialStructures;

    }

    private List<ThoughtParticle> addEnterExitsToStructure(RadialStructure radialStructure, List<ThoughtParticle> enterExitTransitions) {
        List<ThoughtParticle> particlesToRemove = new ArrayList<>();

        for (ThoughtParticle enterExitParticle : enterExitTransitions) {
            Link link = enterExitParticle.getLink();
            LocationInFocus locationA = link.getLocationA();
            LocationInFocus locationB = link.getLocationB();

            LocationInFocus nonEnterExitLocation = getNonEnterExitNode(locationA, locationB);
            if (nonEnterExitLocation == null) {
                //this is a useless exit to enter transition, just delete it
                particlesToRemove.add(enterExitParticle);
            } else if (radialStructure.contains(nonEnterExitLocation)) {
                LocationInFocus enterExitLocation = getEnterExitNode(locationA, locationB);

                if (enterExitLocation == entranceLocation) {
                    radialStructure.addLinkFromEntrance(nonEnterExitLocation, link.getTraversalCount(), enterExitParticle.getFocusWeight(), enterExitParticle.getVelocity());
                } else if (enterExitLocation == exitLocation) {
                    radialStructure.addLinkToExit(nonEnterExitLocation, link.getTraversalCount(), enterExitParticle.getFocusWeight(), enterExitParticle.getVelocity());
                }

                particlesToRemove.add(enterExitParticle);
            }
        }
        return particlesToRemove;
    }

    private LocationInFocus getEnterExitNode(LocationInFocus locationA, LocationInFocus locationB) {
        if (locationA == entranceLocation || locationA == exitLocation) {
            return locationA;
        }
        if (locationB == entranceLocation || locationB == exitLocation) {
            return locationB;
        }

        return null;
    }

    private LocationInFocus getNonEnterExitNode(LocationInFocus locationA, LocationInFocus locationB) {
        if (locationA != entranceLocation && locationA != exitLocation) {
            return locationA;
        }
        if (locationB != entranceLocation && locationB != exitLocation) {
            return locationB;
        }
        return null;
    }

    private RadialStructure createRadialStructureAndRemoveParticlesUsed(List<ThoughtParticle> particlesByWeight) {
        RadialStructure radialStructure = new RadialStructure();

        LocationInFocus centerOfFocus = getCenterOfFocus(particlesByWeight);
        radialStructure.placeCenter(centerOfFocus);
        radialStructure.placeEntrance(entranceLocation);
        radialStructure.placeExit(exitLocation);

        List<ThoughtParticle> firstRingParticles = findConnectedParticles(particlesByWeight, centerOfFocus);
        List<LocationInFocus> firstRingLocations = createFirstRing(radialStructure, firstRingParticles, centerOfFocus);
        particlesByWeight.removeAll(firstRingParticles);

        List<ThoughtParticle> connectionsWithinFirstRing = findParticlesCompletelyWithinRing(particlesByWeight, firstRingLocations);
        createMoreLinksInFirstRing(radialStructure, connectionsWithinFirstRing);
        particlesByWeight.removeAll(connectionsWithinFirstRing);

        List<LocationInFocus> locationsInLastRing = firstRingLocations;
        int lastParticlesRemaining = particlesByWeight.size();

        //add rings until remaining particles are disconnected

        while (particlesByWeight.size() > 0 )  {
            List<ThoughtParticle> connectionsForNextRing = findConnectedParticles(particlesByWeight, locationsInLastRing);
            List<LocationInFocus> nextRingLocations = createNextRing(radialStructure, connectionsForNextRing, locationsInLastRing);
            particlesByWeight.removeAll(connectionsForNextRing);

            List<ThoughtParticle> connectionsWithinNewRing = findParticlesCompletelyWithinRing(particlesByWeight, nextRingLocations);
            createMoreLinksInHighestRing(radialStructure, connectionsWithinNewRing);
            particlesByWeight.removeAll(connectionsWithinNewRing);

            locationsInLastRing = nextRingLocations;

            if (lastParticlesRemaining == particlesByWeight.size()) {
                break;
            } else {
                lastParticlesRemaining = particlesByWeight.size();
            }
        }
        return radialStructure;
    }

    private List<ThoughtParticle> findEnterExitTransitions(List<ThoughtParticle> particlesByWeight) {
        List<ThoughtParticle> enterExitTransitions = new ArrayList<>();
        for (ThoughtParticle particle : particlesByWeight) {
            Link link = particle.getLink();
            LocationInFocus locationA = link.getLocationA();
            LocationInFocus locationB = link.getLocationB();

            if (locationA == entranceLocation || locationB == entranceLocation ||
                    locationA == exitLocation || locationB == exitLocation) {
                enterExitTransitions.add(particle);
            }
        }

        return enterExitTransitions;
    }

    private List<LocationInFocus> createNextRing(RadialStructure radialStructure,
                                                 List<ThoughtParticle> connectionsForNextRing,
                                                 List<LocationInFocus> locationsInLastRing) {
        radialStructure.createNextRing();

        for (ThoughtParticle connectedParticle : connectionsForNextRing) {
            Link link = connectedParticle.getLink();
            LocationInFocus locationA = link.getLocationA();
            LocationInFocus locationB = link.getLocationB();

            LocationInFocus locationToLinkTo = getSourceLocation(locationsInLastRing, locationA, locationB);
            LocationInFocus locationToAdd = getConnectedLocation(locationsInLastRing, locationA, locationB);

            radialStructure.addLocationToHighestRing(locationToLinkTo, locationToAdd, link.getTraversalCount(),
                    connectedParticle.getFocusWeight(), connectedParticle.getVelocity());
        }

        return radialStructure.getLocationsInFirstRing();
    }


    private void createMoreLinksInHighestRing(RadialStructure radialStructure,
                                              List<ThoughtParticle> connectionsWithinHighestRing) {

        for (ThoughtParticle particle : connectionsWithinHighestRing) {
            Link link = particle.getLink();
            LocationInFocus locationA = link.getLocationA();
            LocationInFocus locationB = link.getLocationB();

            radialStructure.addExtraLinkWithinHighestRing(locationA, locationB, link.getTraversalCount(),
                    particle.getFocusWeight(), particle.getVelocity());
        }
    }

    private void createMoreLinksInFirstRing(RadialStructure radialStructure,
                                            List<ThoughtParticle> connectionsWithinFirstRing) {

        for (ThoughtParticle particle : connectionsWithinFirstRing) {
            Link link = particle.getLink();
            LocationInFocus locationA = link.getLocationA();
            LocationInFocus locationB = link.getLocationB();

            radialStructure.addExtraLinkWithinFirstRing(locationA, locationB, link.getTraversalCount(),
                    particle.getFocusWeight(), particle.getVelocity());
        }
    }


    private List<LocationInFocus> createFirstRing(RadialStructure radialStructure,
                                                  List<ThoughtParticle> firstRingParticles,
                                                  LocationInFocus centerOfFocus) {

        for (ThoughtParticle connectedParticle : firstRingParticles) {
            Link link = connectedParticle.getLink();
            LocationInFocus locationA = link.getLocationA();
            LocationInFocus locationB = link.getLocationB();

            LocationInFocus locationToAdd = getConnectedLocation(centerOfFocus, locationA, locationB);

            radialStructure.addLocationToFirstRing(locationToAdd, link.getTraversalCount(),
                    connectedParticle.getFocusWeight(), connectedParticle.getVelocity());

        }

        return radialStructure.getLocationsInFirstRing();
    }

    private List<ThoughtParticle> findParticlesCompletelyWithinRing(List<ThoughtParticle> particlesByWeight,
                                                                    List<LocationInFocus> firstRingLocations) {
        List<ThoughtParticle> particlesInsideRing = new ArrayList<>();

        for (ThoughtParticle particle : particlesByWeight) {
            Link link = particle.getLink();
            LocationInFocus locationA = link.getLocationA();
            LocationInFocus locationB = link.getLocationB();

            if (firstRingLocations.contains(locationA) && firstRingLocations.contains(locationB)) {
                particlesInsideRing.add(particle);
            }
        }

        return particlesInsideRing;
    }

    private LocationInFocus getSourceLocation(List<LocationInFocus> locationsInLastRing,
                                              LocationInFocus locationA, LocationInFocus locationB) {
        if (locationsInLastRing.contains(locationA)) {
            return locationA;
        } else {
            return locationB;
        }

    }

    private LocationInFocus getConnectedLocation(List<LocationInFocus> locationsInLastRing,
                                                 LocationInFocus locationA, LocationInFocus locationB) {
        if (locationsInLastRing.contains(locationA)) {
            return locationB;
        } else {
            return locationA;
        }
    }

    private LocationInFocus getConnectedLocation(LocationInFocus centerOfFocus,
                                                 LocationInFocus locationA, LocationInFocus locationB) {
        if (centerOfFocus == locationA) {
            return locationB;
        } else {
            return locationA;
        }
    }

    private List<ThoughtParticle> findConnectedParticles(List<ThoughtParticle> particlesByWeight,
                                                         List<LocationInFocus> lastRingLocations) {
        List<ThoughtParticle> connectedParticles = new ArrayList<>();

        for (ThoughtParticle particle : particlesByWeight) {
            LocationInFocus locationA = particle.getLink().getLocationA();
            LocationInFocus locationB = particle.getLink().getLocationB();

            if (lastRingLocations.contains(locationA) || lastRingLocations.contains(locationB)) {
                connectedParticles.add(particle);
            }
        }

        return connectedParticles;
    }

    private List<ThoughtParticle> findConnectedParticles(List<ThoughtParticle> particlesByWeight,
                                                         LocationInFocus centerOfFocus) {
        List<ThoughtParticle> connectedParticles = new ArrayList<>();

        for (ThoughtParticle particle : particlesByWeight) {
            LocationInFocus locationA = particle.getLink().getLocationA();
            LocationInFocus locationB = particle.getLink().getLocationB();

            if ((centerOfFocus == locationA || centerOfFocus == locationB)) {
                connectedParticles.add(particle);
            }
        }

        return connectedParticles;
    }

    private LocationInFocus findOrCreateLocation(String locationPath) {
        LocationInFocus location = locationMap.get(locationPath);
        if (location == null) {
            location = new LocationInFocus(this.focalPoint, locationPath, locationIndex++);
            locationMap.put(locationPath, location);
        }
        return location;
    }

    private LocationInFocus getCenterOfFocus(List<ThoughtParticle> particlesByWeight) {

        LocationInFocus center = null;

        if (particlesByWeight.size() > 0) {
            ThoughtParticle heaviest = particlesByWeight.get(0);

            Link link = heaviest.getLink();

            Duration timeInLocationA = link.getLocationA().getTotalTimeInvestment();
            Duration timeInLocationB = link.getLocationB().getTotalTimeInvestment();

            if (timeInLocationA.compareTo(timeInLocationB) > 0) {
                center = link.getLocationA();
            } else {
                center = link.getLocationB();
            }
        }

        return center;
    }


    private List<ThoughtParticle> getNormalizedParticlesSortedByWeight() {
        List<ThoughtParticle> particles = new ArrayList<>(thoughtParticleMap.values());

        Collections.sort(particles);

        double maxWeight = getMaxWeight(particles);
        double minWeight = getMinWeight(particles);

        for (ThoughtParticle particle : particles) {
            particle.normalizeWeight(minWeight, maxWeight);
        }

        double maxVelocity = getMaxVelocity(particles);
        double minVelocity = getMinVelocity(particles);

        for (ThoughtParticle particle : particles) {
            particle.normalizeVelocity(minVelocity, maxVelocity);
        }

        return particles;
    }

    private double getMaxWeight(List<ThoughtParticle> particles) {
        double max = 1;
        if (particles.size() > 0) {
            max = particles.get(0).getFocusWeight();
        }
        return max;
    }

    private double getMinWeight(List<ThoughtParticle> particles) {
        double min = 0;
        if (particles.size() > 1) {
            min = particles.get(particles.size() - 1).getFocusWeight();
        }
        return min;
    }

    private double getMaxVelocity(List<ThoughtParticle> particles) {
        double max = 0;
        for (ThoughtParticle particle : particles) {
            if (particle.getVelocity() > max) {
                max = particle.getVelocity();
            }
        }
        return max;
    }

    private double getMinVelocity(List<ThoughtParticle> particles) {
        double min = Double.MAX_VALUE;
        for (ThoughtParticle particle : particles) {
            if (particle.getVelocity() < min) {
                min = particle.getVelocity();
            }
        }
        return min;
    }

    private ThoughtParticle findOrCreateParticle(Link edge) {

        String particleKey = edge.toKey();
        ThoughtParticle particle = thoughtParticleMap.get(particleKey);

        if (particle == null) {
            particle = new ThoughtParticle(edge);
            thoughtParticleMap.put(particleKey, particle);
        }
        return particle;
    }

    public void separateEntranceAndExitFromInsides(String entranceOfPlace, String exitOfPlace) {

    }


    private class DecayingGrowthRate {
        private final long scaleRelativeToTime;
        double growthRate = 1;
        double decayRate = 1;
        int risingDenominator = 1;

        DecayingGrowthRate(Duration scaleRelativeToTime) {
            this.scaleRelativeToTime = scaleRelativeToTime.getSeconds();
        }

        void decay() {
            risingDenominator++;

            this.growthRate = 1.0 / risingDenominator;
            this.decayRate = 1.0 / risingDenominator;
        }

        double calculateGrowthFor(double timeInSeconds) {
            if (scaleRelativeToTime >= 0) {
                return growthRate * ((1.0 * timeInSeconds) / scaleRelativeToTime);
            } else {
                return 0;
            }
        }

        public double calculateDecayFor(double timeInSeconds) {
            if (scaleRelativeToTime >= 0) {
                return decayRate * ((1.0 * timeInSeconds) / scaleRelativeToTime);
            } else {
                return 0;
            }
        }
    }

    private class ThoughtParticle implements Comparable<ThoughtParticle> {
        private final Link link;
        private double weight;
        private double normalizedWeight;
        private double velocityOfTransition;
        private double normalizedVelocity;

        ThoughtParticle(Link link) {
            this.link = link;
            this.weight = 0;
        }

        void setVelocityOfTransition(Duration velocityOfTransition) {
            this.velocityOfTransition = velocityOfTransition.getSeconds();
        }

        void growHeavyWithFocus(DecayingGrowthRate decayingGrowthRate, Duration timeInLocation) {
            this.weight += Math.sqrt(timeInLocation.getSeconds()) * decayingGrowthRate.calculateGrowthFor(velocityOfTransition);
        }


        void decayWithFocusElsewhere(DecayingGrowthRate decayingGrowth) {
            weight -= Math.sqrt(weight) * decayingGrowth.calculateDecayFor(velocityOfTransition);
        }

        void normalizeWeight(double minWeight, double maxWeight) {
            if ((maxWeight - minWeight) > 0) {
                normalizedWeight = (this.weight - minWeight) / (maxWeight - minWeight);
            }
        }

        void normalizeVelocity(double minVelocity, double maxVelocity) {
            if ((maxVelocity - minVelocity) > 0) {
                normalizedVelocity = (this.velocityOfTransition - minVelocity) / (maxVelocity - minVelocity);
            }
        }

        double getFocusWeight() {
            if (normalizedWeight > 0) {
                return normalizedWeight;
            }
            return weight;
        }

        @Override
        public int compareTo(ThoughtParticle o) {
            return Double.compare(weight, o.weight) * -1;
        }

        public Link getLink() {
            return link;
        }

        double getVelocity() {
            if (normalizedVelocity > 0) {
                return normalizedVelocity;
            }
            return velocityOfTransition;
        }

    }


    private Link findOrCreateEdge(LocationInFocus locationA, LocationInFocus locationB) {

        String linkKey = createLinkKeyIgnoringOrder(locationA, locationB);

        Link link = linkMap.get(linkKey);

        if (link == null) {
            link = new Link(locationA, locationB);
            linkMap.put(linkKey, link);
        }

        return link;
    }

    private String createLinkKeyIgnoringOrder(LocationInFocus locationA, LocationInFocus locationB) {
        String pathA = locationA.toKey();
        String pathB = locationB.toKey();

        if (pathA.compareTo(pathB) > 0) {
            pathA = locationB.getLocationPath();
            pathB = locationA.getLocationPath();
        }

        return pathA + pathB;
    }


    private class Link {

        private final LocationInFocus locationA;
        private final LocationInFocus locationB;
        private int visitCounter;

        Link(LocationInFocus locationA, LocationInFocus locationB) {
            this.locationA = locationA;
            this.locationB = locationB;
            this.visitCounter = 0;
        }

        LocationInFocus getLocationA() {
            return locationA;
        }

        LocationInFocus getLocationB() {
            return locationB;
        }

        void visit() {
            this.visitCounter++;
        }

        String toKey() {
            return locationA.toKey() + "=>" + locationB.toKey();
        }

        int getTraversalCount() {
            return visitCounter;
        }
    }

}
