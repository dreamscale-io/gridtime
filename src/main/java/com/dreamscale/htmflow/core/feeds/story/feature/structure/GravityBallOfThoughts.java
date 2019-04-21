package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGrid;

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

    private final Box box;
    private final FeatureFactory featureFactory;
    private final StoryGrid storyGrid;

    private Map<UUID, ThoughtParticle> thoughtParticleMap = new HashMap<>();

    private LinkedList<ThoughtParticle> thoughtTracer = new LinkedList<>();

    private LocationInBox currentLocation;
    private Traversal lastTraversal;

    private static final int TRACER_LENGTH = 5;

    private static final String ENTRANCE_OF_BOX = "[entrance]";
    private static final String EXIT_OF_BOX = "[exit]";

    private LocationInBox exitLocation;
    private LocationInBox entranceLocation;


    public GravityBallOfThoughts(StoryGrid storyGrid, FeatureFactory featureFactory, Box box) {
        this.storyGrid = storyGrid;
        this.featureFactory = featureFactory;
        this.box = box;

        this.entranceLocation = featureFactory.findOrCreateLocation(box.getBoxName(), ENTRANCE_OF_BOX);
        this.currentLocation = entranceLocation;
    }

    public void initLocation(LocationInBox lastLocationInBox) {
        this.currentLocation = lastLocationInBox;
    }

    public LocationInBox gotoLocationInSpace(String locationPath) {

        LocationInBox fromLocation = currentLocation;
        LocationInBox toLocation = featureFactory.findOrCreateLocation(box.getBoxName(), locationPath);

        currentLocation = toLocation;

        if (fromLocation != toLocation) {
            addThoughtParticleForTraversal(fromLocation, toLocation);
        }

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
        if (thoughtTracer.isEmpty()) return;

        thoughtTracer.get(0).addVelocitySample(timeInLocation);

        DecayingGrowthRate decayingGrowth = new DecayingGrowthRate(timeInLocation);

        //pushing a new thought particle on the tracer, also has a side-effect of decreasing the weight
        //of ALL existing particles by 1 next of decay, for any particles linked in the active tracer,
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

        Traversal traversal = featureFactory.findOrCreateTraversal(fromLocation, toLocation);
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

    public BoxActivity createBoxOfThoughtBubbles() {

        BoxActivity boxActivity = featureFactory.createBoxActivity(this.box);

        List<ThoughtParticle> particlesByWeight = getNormalizedParticlesSortedByWeight();

        List<ThoughtParticle> enterExitTransitions = findEnterExitTransitions(particlesByWeight);
        particlesByWeight.removeAll(enterExitTransitions);

        if (particlesByWeight.size() == 0 && enterExitTransitions.size() > 0) {
            //in this case, we only have enter/exits, make a thought bubble with what we've got

            ThoughtBubble thoughtBubble = createBubbleOutOfEnterExitNodes(boxActivity, enterExitTransitions);
            addEnterExitsToStructure(thoughtBubble, enterExitTransitions);

            featureFactory.assignAllRingUris(thoughtBubble);
            thoughtBubble.finish();

            boxActivity.addBubble(thoughtBubble);
        }


        //if I've got left over stuff after first loop, these are disconnected networks with alt-centers,
        // so make radial structures for each network

        int lastParticlesRemaining = particlesByWeight.size();

        while (particlesByWeight.size() > 0) {

            ThoughtBubble thoughtBubble = createRadialStructureAndRemoveParticlesUsed(boxActivity, particlesByWeight);

            List<ThoughtParticle> particlesUsed = addEnterExitsToStructure(thoughtBubble, enterExitTransitions);
            enterExitTransitions.removeAll(particlesUsed);
            particlesByWeight.removeAll(particlesUsed);

            featureFactory.assignAllRingUris(thoughtBubble);
            thoughtBubble.finish();

            boxActivity.addBubble(thoughtBubble);

            //we should deplete our particles, but just in case, make sure we don't loop forever
            if (lastParticlesRemaining == particlesByWeight.size()) {
                break;
            } else {
                lastParticlesRemaining = particlesByWeight.size();
            }
        }

        return boxActivity;

    }

    private List<ThoughtParticle> addEnterExitsToStructure(ThoughtBubble thoughtBubble, List<ThoughtParticle> enterExitTransitions) {
        List<ThoughtParticle> particlesToRemove = new ArrayList<>();

        for (ThoughtParticle enterExitParticle : enterExitTransitions) {
            Traversal traversal = enterExitParticle.getLink();
            LocationInBox locationA = traversal.getLocationA();
            LocationInBox locationB = traversal.getLocationB();

            LocationInBox nonEnterExitLocation = getNonEnterExitNode(locationA, locationB);
            if (nonEnterExitLocation == null) {
                //this is a useless exit to enter transition, just delete it
                particlesToRemove.add(enterExitParticle);
            } else if (thoughtBubble.contains(nonEnterExitLocation)) {
                LocationInBox enterExitLocation = getEnterExitNode(locationA, locationB);

                storyGrid.getMetricsFor(traversal).addFocusWeightSample(enterExitParticle.getFocusWeight());
                if (enterExitLocation == entranceLocation) {
                    thoughtBubble.addLinkFromEntrance(nonEnterExitLocation, traversal);
                } else if (enterExitLocation == exitLocation) {
                    thoughtBubble.addLinkToExit(nonEnterExitLocation, traversal);
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

    private ThoughtBubble createBubbleOutOfEnterExitNodes(BoxActivity box, List<ThoughtParticle> enterExitParticles) {
        ThoughtBubble thoughtBubble = featureFactory.createBubbleInsideBox(box);

        LocationInBox centerOfFocus = getCenterOfFocusWithinEnterExits(enterExitParticles);

        thoughtBubble.placeCenter(centerOfFocus);
        thoughtBubble.placeEntrance(entranceLocation);
        thoughtBubble.placeExit(exitLocation);

        return thoughtBubble;
    }



    private ThoughtBubble createRadialStructureAndRemoveParticlesUsed(BoxActivity box, List<ThoughtParticle> particlesByWeight) {
        ThoughtBubble thoughtBubble = featureFactory.createBubbleInsideBox(box);

        LocationInBox centerOfFocus = getCenterOfFocus(particlesByWeight);
        thoughtBubble.placeCenter(centerOfFocus);
        thoughtBubble.placeEntrance(entranceLocation);
        thoughtBubble.placeExit(exitLocation);

        List<ThoughtParticle> firstRingParticles = findConnectedParticles(particlesByWeight, centerOfFocus);
        List<LocationInBox> firstRingLocations = createFirstRing(thoughtBubble, firstRingParticles, centerOfFocus);
        particlesByWeight.removeAll(firstRingParticles);

        List<ThoughtParticle> connectionsWithinFirstRing = findParticlesCompletelyWithinRing(particlesByWeight, firstRingLocations);
        createMoreLinksInFirstRing(thoughtBubble, connectionsWithinFirstRing);
        particlesByWeight.removeAll(connectionsWithinFirstRing);

        List<LocationInBox> locationsInLastRing = firstRingLocations;
        int lastParticlesRemaining = particlesByWeight.size();

        //add rings until remaining particles are disconnected

        while (particlesByWeight.size() > 0 )  {
            List<ThoughtParticle> connectionsForNextRing = findConnectedParticles(particlesByWeight, locationsInLastRing);
            List<LocationInBox> nextRingLocations = createNextRing(thoughtBubble, connectionsForNextRing, locationsInLastRing);
            particlesByWeight.removeAll(connectionsForNextRing);

            List<ThoughtParticle> connectionsWithinNewRing = findParticlesCompletelyWithinRing(particlesByWeight, nextRingLocations);
            createMoreLinksInHighestRing(thoughtBubble, connectionsWithinNewRing);
            particlesByWeight.removeAll(connectionsWithinNewRing);

            locationsInLastRing = nextRingLocations;

            if (lastParticlesRemaining == particlesByWeight.size()) {
                break;
            } else {
                lastParticlesRemaining = particlesByWeight.size();
            }
        }
        return thoughtBubble;
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

    private List<LocationInBox> createNextRing(ThoughtBubble thoughtBubble,
                                               List<ThoughtParticle> connectionsForNextRing,
                                               List<LocationInBox> locationsInLastRing) {
        thoughtBubble.createNextRing();

        for (ThoughtParticle connectedParticle : connectionsForNextRing) {
            Traversal traversal = connectedParticle.getLink();
            LocationInBox locationA = traversal.getLocationA();
            LocationInBox locationB = traversal.getLocationB();

            LocationInBox locationToLinkTo = getSourceLocation(locationsInLastRing, locationA, locationB);
            LocationInBox locationToAdd = getConnectedLocation(locationsInLastRing, locationA, locationB);

            storyGrid.getMetricsFor(traversal).addFocusWeightSample(connectedParticle.getFocusWeight());

            thoughtBubble.addLocationToHighestRing(locationToLinkTo, locationToAdd, traversal);
        }

        return thoughtBubble.getLocationsInFirstRing();
    }


    private void createMoreLinksInHighestRing(ThoughtBubble thoughtBubble,
                                              List<ThoughtParticle> connectionsWithinHighestRing) {

        for (ThoughtParticle particle : connectionsWithinHighestRing) {
            Traversal traversal = particle.getLink();
            LocationInBox locationA = traversal.getLocationA();
            LocationInBox locationB = traversal.getLocationB();

            storyGrid.getMetricsFor(traversal).addFocusWeightSample(particle.getFocusWeight());

            thoughtBubble.addExtraLinkWithinHighestRing(locationA, locationB, traversal);
        }
    }

    private void createMoreLinksInFirstRing(ThoughtBubble thoughtBubble,
                                            List<ThoughtParticle> connectionsWithinFirstRing) {

        for (ThoughtParticle particle : connectionsWithinFirstRing) {
            Traversal traversal = particle.getLink();
            LocationInBox locationA = traversal.getLocationA();
            LocationInBox locationB = traversal.getLocationB();

            storyGrid.getMetricsFor(traversal).addFocusWeightSample(particle.getFocusWeight());

            thoughtBubble.addExtraLinkWithinFirstRing(locationA, locationB, traversal);
        }
    }


    private List<LocationInBox> createFirstRing(ThoughtBubble thoughtBubble,
                                                List<ThoughtParticle> firstRingParticles,
                                                LocationInBox centerOfFocus) {

        for (ThoughtParticle connectedParticle : firstRingParticles) {
            Traversal traversal = connectedParticle.getLink();
            LocationInBox locationA = traversal.getLocationA();
            LocationInBox locationB = traversal.getLocationB();

            LocationInBox locationToAdd = getConnectedLocation(centerOfFocus, locationA, locationB);

            storyGrid.getMetricsFor(traversal).addFocusWeightSample(connectedParticle.getFocusWeight());

            thoughtBubble.addLocationToFirstRing(locationToAdd, traversal);

        }

        return thoughtBubble.getLocationsInFirstRing();
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


    private LocationInBox getCenterOfFocus(List<ThoughtParticle> particlesByWeight) {

        LocationInBox center = null;

        if (particlesByWeight.size() > 0) {
            ThoughtParticle heaviest = particlesByWeight.get(0);

            Traversal link = heaviest.getLink();

            Duration timeInLocationA = storyGrid.getMetricsFor(link.getLocationA()).getTotalTimeInvestment();
            Duration timeInLocationB = storyGrid.getMetricsFor(link.getLocationB()).getTotalTimeInvestment();

            if (timeInLocationA.compareTo(timeInLocationB) > 0) {
                center = link.getLocationA();
            } else {
                center = link.getLocationB();
            }
        }

        return center;
    }

    private LocationInBox getCenterOfFocusWithinEnterExits(List<ThoughtParticle> enterExitParticles) {
        LocationInBox center = null;

        if (enterExitParticles.size() > 0) {
            Traversal link = enterExitParticles.get(0).getLink();

            center = getNonEnterExitNode(link.getLocationA(), link.getLocationB());
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

    private ThoughtParticle findOrCreateParticle(Traversal traversal) {

        ThoughtParticle particle = thoughtParticleMap.get(traversal.getId());

        if (particle == null) {
            particle = new ThoughtParticle(traversal);
            thoughtParticleMap.put(traversal.getId(), particle);
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
        private int traversalCount;

        private double normalizedVelocity;

        ThoughtParticle(Traversal link) {
            this.link = link;
            this.weight = 0;
            traversalCount = 0;
        }

        void addVelocitySample(Duration velocityOfTransition) {
            velocityWeightedAvg = (( velocityWeightedAvg * traversalCount) + velocityOfTransition.getSeconds()) / (traversalCount + 1);
            traversalCount++;
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





}
