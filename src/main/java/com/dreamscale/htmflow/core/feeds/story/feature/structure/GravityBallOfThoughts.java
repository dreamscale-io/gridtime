package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.StandardizedKeyMapper;

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

    private final Map<String, LocationInBox> locationMap = new HashMap<>();
    private final Map<String, Traversal> traversalMap = new HashMap<>();
    private final FocalPoint focalPoint;

    private Map<String, ThoughtParticle> thoughtParticleMap = new HashMap<>();
    private LinkedList<ThoughtParticle> thoughtTracer = new LinkedList<>();

    private LocationInBox currentLocation;
    private Traversal lastTraversal;

    private static final int TRACER_LENGTH = 5;

    private static final String ENTRANCE_OF_BOX = "[entrance]";
    private static final String EXIT_OF_BOX = "[exit]";

    private LocationInBox exitLocation;
    private LocationInBox entranceLocation;


    public GravityBallOfThoughts(FocalPoint focalPoint) {
        this.focalPoint = focalPoint;
    }

    public void initStartingLocation(String locationPath) {
        currentLocation = findOrCreateLocation(locationPath);
    }

    public LocationInBox gotoLocationInSpace(String locationPath) {

        LocationInBox fromLocation = currentLocation;
        LocationInBox toLocation = findOrCreateLocation(locationPath);

        currentLocation = toLocation;

        addThoughtParticleForTraversal(fromLocation, toLocation);

        return toLocation;
    }

    public LocationInBox gotoExit() {
        this.exitLocation = gotoLocationInSpace(EXIT_OF_BOX);
        return this.exitLocation;
    }

    public LocationInBox gotoEntrance() {
        this.entranceLocation = gotoLocationInSpace(ENTRANCE_OF_BOX);
        return this.entranceLocation;
    }


    public LocationInBox getCurrentLocation() {
        return currentLocation;
    }

    public void growHeavyWithFocus(Duration timeInLocation) {
        focalPoint.getBox().spendTime(timeInLocation);
        currentLocation.spendTime(timeInLocation);
        lastTraversal.spendTime(timeInLocation);

        thoughtTracer.get(0).addVelocitySample(timeInLocation);

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

    private void addThoughtParticleForTraversal(LocationInBox fromLocation, LocationInBox toLocation) {

        Traversal traversal = findOrCreateEdge(fromLocation, toLocation);
        ThoughtParticle particle = findOrCreateParticle(traversal);

        pushThoughtParticleOntoTracer(particle);

        lastTraversal = traversal;
    }

    private void pushThoughtParticleOntoTracer(ThoughtParticle particle) {
        thoughtTracer.push(particle);

        if (thoughtTracer.size() > TRACER_LENGTH) {
            thoughtTracer.removeLast();
        }

    }

    public List<ThoughtBubble> extractThoughtBubbles() {

        List<ThoughtParticle> particlesByWeight = getNormalizedParticlesSortedByWeight();

        List<ThoughtParticle> enterExitTransitions = findEnterExitTransitions(particlesByWeight);
        particlesByWeight.removeAll(enterExitTransitions);

        List<ThoughtBubble> thoughtBubbles = new ArrayList<>();

        //if I've got left over stuff after a loop, these are disconnected networks with alt-centers,
        // so make radial structures for each network

        int lastParticlesRemaining = particlesByWeight.size();

        while (particlesByWeight.size() > 0) {

            RadialStructure radialStructure = createRadialStructureAndRemoveParticlesUsed(particlesByWeight);

            List<ThoughtParticle> particlesUsed = addEnterExitsToStructure(radialStructure, enterExitTransitions);
            enterExitTransitions.removeAll(particlesUsed);
            particlesByWeight.removeAll(particlesUsed);

            thoughtBubbles.add(new ThoughtBubble(radialStructure));

            //we should deplete our particles, but just in case, make sure we don't loop forever
            if (lastParticlesRemaining == particlesByWeight.size()) {
                break;
            } else {
                lastParticlesRemaining = particlesByWeight.size();
            }
        }

        return thoughtBubbles;

    }

    private List<ThoughtParticle> addEnterExitsToStructure(RadialStructure radialStructure, List<ThoughtParticle> enterExitTransitions) {
        List<ThoughtParticle> particlesToRemove = new ArrayList<>();

        for (ThoughtParticle enterExitParticle : enterExitTransitions) {
            Traversal traversal = enterExitParticle.getLink();
            LocationInBox locationA = traversal.getLocationA();
            LocationInBox locationB = traversal.getLocationB();

            LocationInBox nonEnterExitLocation = getNonEnterExitNode(locationA, locationB);
            if (nonEnterExitLocation == null) {
                //this is a useless exit to enter transition, just delete it
                particlesToRemove.add(enterExitParticle);
            } else if (radialStructure.contains(nonEnterExitLocation)) {
                LocationInBox enterExitLocation = getEnterExitNode(locationA, locationB);

                if (enterExitLocation == entranceLocation) {
                    radialStructure.addLinkFromEntrance(nonEnterExitLocation, traversal, enterExitParticle.getFocusWeight(), enterExitParticle.getVelocity());
                } else if (enterExitLocation == exitLocation) {
                    radialStructure.addLinkToExit(nonEnterExitLocation, traversal, enterExitParticle.getFocusWeight(), enterExitParticle.getVelocity());
                }

                particlesToRemove.add(enterExitParticle);
            }
        }
        return particlesToRemove;
    }

    private LocationInBox getEnterExitNode(LocationInBox locationA, LocationInBox locationB) {
        if (locationA == entranceLocation || locationA == exitLocation) {
            return locationA;
        }
        if (locationB == entranceLocation || locationB == exitLocation) {
            return locationB;
        }

        return null;
    }

    private LocationInBox getNonEnterExitNode(LocationInBox locationA, LocationInBox locationB) {
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

        LocationInBox centerOfFocus = getCenterOfFocus(particlesByWeight);
        radialStructure.placeCenter(centerOfFocus);
        radialStructure.placeEntrance(entranceLocation);
        radialStructure.placeExit(exitLocation);

        List<ThoughtParticle> firstRingParticles = findConnectedParticles(particlesByWeight, centerOfFocus);
        List<LocationInBox> firstRingLocations = createFirstRing(radialStructure, firstRingParticles, centerOfFocus);
        particlesByWeight.removeAll(firstRingParticles);

        List<ThoughtParticle> connectionsWithinFirstRing = findParticlesCompletelyWithinRing(particlesByWeight, firstRingLocations);
        createMoreLinksInFirstRing(radialStructure, connectionsWithinFirstRing);
        particlesByWeight.removeAll(connectionsWithinFirstRing);

        List<LocationInBox> locationsInLastRing = firstRingLocations;
        int lastParticlesRemaining = particlesByWeight.size();

        //add rings until remaining particles are disconnected

        while (particlesByWeight.size() > 0 )  {
            List<ThoughtParticle> connectionsForNextRing = findConnectedParticles(particlesByWeight, locationsInLastRing);
            List<LocationInBox> nextRingLocations = createNextRing(radialStructure, connectionsForNextRing, locationsInLastRing);
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
            Traversal link = particle.getLink();
            LocationInBox locationA = link.getLocationA();
            LocationInBox locationB = link.getLocationB();

            if (locationA == entranceLocation || locationB == entranceLocation ||
                    locationA == exitLocation || locationB == exitLocation) {
                enterExitTransitions.add(particle);
            }
        }

        return enterExitTransitions;
    }

    private List<LocationInBox> createNextRing(RadialStructure radialStructure,
                                               List<ThoughtParticle> connectionsForNextRing,
                                               List<LocationInBox> locationsInLastRing) {
        radialStructure.createNextRing();

        for (ThoughtParticle connectedParticle : connectionsForNextRing) {
            Traversal traversal = connectedParticle.getLink();
            LocationInBox locationA = traversal.getLocationA();
            LocationInBox locationB = traversal.getLocationB();

            LocationInBox locationToLinkTo = getSourceLocation(locationsInLastRing, locationA, locationB);
            LocationInBox locationToAdd = getConnectedLocation(locationsInLastRing, locationA, locationB);

            radialStructure.addLocationToHighestRing(locationToLinkTo, locationToAdd, traversal,
                    connectedParticle.getFocusWeight(), connectedParticle.getVelocity());
        }

        return radialStructure.getLocationsInFirstRing();
    }


    private void createMoreLinksInHighestRing(RadialStructure radialStructure,
                                              List<ThoughtParticle> connectionsWithinHighestRing) {

        for (ThoughtParticle particle : connectionsWithinHighestRing) {
            Traversal traversal = particle.getLink();
            LocationInBox locationA = traversal.getLocationA();
            LocationInBox locationB = traversal.getLocationB();

            radialStructure.addExtraLinkWithinHighestRing(locationA, locationB, traversal,
                    particle.getFocusWeight(), particle.getVelocity());
        }
    }

    private void createMoreLinksInFirstRing(RadialStructure radialStructure,
                                            List<ThoughtParticle> connectionsWithinFirstRing) {

        for (ThoughtParticle particle : connectionsWithinFirstRing) {
            Traversal traversal = particle.getLink();
            LocationInBox locationA = traversal.getLocationA();
            LocationInBox locationB = traversal.getLocationB();

            radialStructure.addExtraLinkWithinFirstRing(locationA, locationB, traversal,
                    particle.getFocusWeight(), particle.getVelocity());
        }
    }


    private List<LocationInBox> createFirstRing(RadialStructure radialStructure,
                                                List<ThoughtParticle> firstRingParticles,
                                                LocationInBox centerOfFocus) {

        for (ThoughtParticle connectedParticle : firstRingParticles) {
            Traversal traversal = connectedParticle.getLink();
            LocationInBox locationA = traversal.getLocationA();
            LocationInBox locationB = traversal.getLocationB();

            LocationInBox locationToAdd = getConnectedLocation(centerOfFocus, locationA, locationB);

            radialStructure.addLocationToFirstRing(locationToAdd, traversal,
                    connectedParticle.getFocusWeight(), connectedParticle.getVelocity());

        }

        return radialStructure.getLocationsInFirstRing();
    }

    private List<ThoughtParticle> findParticlesCompletelyWithinRing(List<ThoughtParticle> particlesByWeight,
                                                                    List<LocationInBox> firstRingLocations) {
        List<ThoughtParticle> particlesInsideRing = new ArrayList<>();

        for (ThoughtParticle particle : particlesByWeight) {
            Traversal link = particle.getLink();
            LocationInBox locationA = link.getLocationA();
            LocationInBox locationB = link.getLocationB();

            if (firstRingLocations.contains(locationA) && firstRingLocations.contains(locationB)) {
                particlesInsideRing.add(particle);
            }
        }

        return particlesInsideRing;
    }

    private LocationInBox getSourceLocation(List<LocationInBox> locationsInLastRing,
                                            LocationInBox locationA, LocationInBox locationB) {
        if (locationsInLastRing.contains(locationA)) {
            return locationA;
        } else {
            return locationB;
        }

    }

    private LocationInBox getConnectedLocation(List<LocationInBox> locationsInLastRing,
                                               LocationInBox locationA, LocationInBox locationB) {
        if (locationsInLastRing.contains(locationA)) {
            return locationB;
        } else {
            return locationA;
        }
    }

    private LocationInBox getConnectedLocation(LocationInBox centerOfFocus,
                                               LocationInBox locationA, LocationInBox locationB) {
        if (centerOfFocus == locationA) {
            return locationB;
        } else {
            return locationA;
        }
    }

    private List<ThoughtParticle> findConnectedParticles(List<ThoughtParticle> particlesByWeight,
                                                         List<LocationInBox> lastRingLocations) {
        List<ThoughtParticle> connectedParticles = new ArrayList<>();

        for (ThoughtParticle particle : particlesByWeight) {
            LocationInBox locationA = particle.getLink().getLocationA();
            LocationInBox locationB = particle.getLink().getLocationB();

            if (lastRingLocations.contains(locationA) || lastRingLocations.contains(locationB)) {
                connectedParticles.add(particle);
            }
        }

        return connectedParticles;
    }

    private List<ThoughtParticle> findConnectedParticles(List<ThoughtParticle> particlesByWeight,
                                                         LocationInBox centerOfFocus) {
        List<ThoughtParticle> connectedParticles = new ArrayList<>();

        for (ThoughtParticle particle : particlesByWeight) {
            LocationInBox locationA = particle.getLink().getLocationA();
            LocationInBox locationB = particle.getLink().getLocationB();

            if ((centerOfFocus == locationA || centerOfFocus == locationB)) {
                connectedParticles.add(particle);
            }
        }

        return connectedParticles;
    }

    private LocationInBox findOrCreateLocation(String locationPath) {
        LocationInBox location = locationMap.get(locationPath);
        if (location == null) {
            location = new LocationInBox(this.focalPoint, locationPath);
            locationMap.put(locationPath, location);
        }
        return location;
    }

    private LocationInBox getCenterOfFocus(List<ThoughtParticle> particlesByWeight) {

        LocationInBox center = null;

        if (particlesByWeight.size() > 0) {
            ThoughtParticle heaviest = particlesByWeight.get(0);

            Traversal link = heaviest.getLink();

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

    private ThoughtParticle findOrCreateParticle(Traversal edge) {

        String particleKey = edge.toKey();
        ThoughtParticle particle = thoughtParticleMap.get(particleKey);

        if (particle == null) {
            particle = new ThoughtParticle(edge);
            thoughtParticleMap.put(particleKey, particle);
        }
        return particle;
    }

    public Traversal getLastTraversal() {
        return lastTraversal;
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
        private final Traversal link;
        private double weight;
        private double normalizedWeight;
        private double velocityWeightedAvg;
        private int velocitySampleCount;

        private double normalizedVelocity;

        ThoughtParticle(Traversal link) {
            this.link = link;
            this.weight = 0;
            velocitySampleCount = 0;
        }

        void addVelocitySample(Duration velocityOfTransition) {
            velocityWeightedAvg = (( velocityWeightedAvg * velocitySampleCount ) + velocityOfTransition.getSeconds()) / (velocitySampleCount + 1);
            velocitySampleCount++;
        }

        void growHeavyWithFocus(DecayingGrowthRate decayingGrowthRate, Duration timeInLocation) {
            this.weight += Math.sqrt(timeInLocation.getSeconds()) * decayingGrowthRate.calculateGrowthFor(velocityWeightedAvg);
        }


        void decayWithFocusElsewhere(DecayingGrowthRate decayingGrowth) {
            weight -= Math.sqrt(weight) * decayingGrowth.calculateDecayFor(velocityWeightedAvg);
        }

        void normalizeWeight(double minWeight, double maxWeight) {
            if ((maxWeight - minWeight) > 0) {
                normalizedWeight = (this.weight - minWeight) / (maxWeight - minWeight);
            }
        }

        void normalizeVelocity(double minVelocity, double maxVelocity) {
            if ((maxVelocity - minVelocity) > 0) {
                normalizedVelocity = (this.velocityWeightedAvg - minVelocity) / (maxVelocity - minVelocity);
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

        public Traversal getLink() {
            return link;
        }

        double getVelocity() {
            if (normalizedVelocity > 0) {
                return normalizedVelocity;
            }
            return velocityWeightedAvg;
        }

    }


    private Traversal findOrCreateEdge(LocationInBox locationA, LocationInBox locationB) {

        String traversalKey = StandardizedKeyMapper.createLocationTraversalKey(locationA.toKey(), locationB.toKey());

        Traversal traversal = traversalMap.get(traversalKey);

        if (traversal == null) {
            traversal = new Traversal(locationA, locationB);
            traversalMap.put(traversalKey, traversal);
        }

        return traversal;
    }



}
