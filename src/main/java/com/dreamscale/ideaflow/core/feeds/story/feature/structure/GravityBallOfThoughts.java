package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import java.time.Duration;
import java.util.*;

/**
 * Create a 2D structural model of a FocalPoint by first identifying the "focal center",
 * then orienting positions of elements in 2D space as a radial map, relative to focal center,
 * with the current "velocity" based on the amount of time spent during the last direct access
 */

public class GravityBallOfThoughts {

    private final Map<String, LocationInThought> locationMap = new HashMap<>();
    private final Map<String, Link> linkMap = new HashMap<>();
    private final FocalPoint focalPoint;

    private Map<String, ThoughtParticle> thoughtParticleMap = new HashMap<>();
    private LinkedList<ThoughtParticle> thoughtTracer = new LinkedList<>();

    private LocationInThought currentLocation;
    private int locationIndex = 1;

    private static final int TRACER_LENGTH = 5;

    public GravityBallOfThoughts(FocalPoint focalPoint) {
        this.focalPoint = focalPoint;
    }

    public LocationInThought gotoLocationInSpace(String locationPath) {

        LocationInThought fromLocation = currentLocation;
        LocationInThought toLocation = findOrCreateLocation(locationPath);

        toLocation.visit();

        addThoughtParticleForTraversal(fromLocation, toLocation);

        return toLocation;
    }

    public void growHeavyWithFocus(Duration timeInLocation) {
        currentLocation.spendTime(timeInLocation);


        DecayingGrowthRate decayingGrowth = new DecayingGrowthRate(thoughtTracer);
        thoughtTracer.get(0).setSpeedOfThought(timeInLocation);

        for (ThoughtParticle particle : thoughtTracer) {
            particle.growHeavyWithFocus(decayingGrowth.getRate(), timeInLocation);

            decayingGrowth.decay();
        }
    }

    private void addThoughtParticleForTraversal(LocationInThought fromLocation, LocationInThought toLocation) {

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

    RadialStructure buildRadialStructure() {
        RadialStructure radialStructure = new RadialStructure();

        List<ThoughtParticle> particlesByWeight = getNormalizedParticlesSortedByWeight();

        LocationInThought centerOfFocus = getCenterOfFocus(particlesByWeight);
        radialStructure.placeCenter(centerOfFocus);

        List<ThoughtParticle> firstRingParticles = findConnectedParticles(particlesByWeight, centerOfFocus);
        List<LocationInThought> firstRingLocations = createFirstRing(radialStructure, firstRingParticles, centerOfFocus);
        particlesByWeight.removeAll(firstRingParticles);

        List<ThoughtParticle> connectionsWithinFirstRing = findParticlesCompletelyWithinRing(particlesByWeight, firstRingLocations);
        createMoreLinksInFirstRing(radialStructure, connectionsWithinFirstRing);
        particlesByWeight.removeAll(connectionsWithinFirstRing);

        List<LocationInThought> locationsInLastRing = firstRingLocations;
        int lastParticleCount = particlesByWeight.size();

        while (!particlesByWeight.isEmpty()) {
            List<ThoughtParticle> connectionsForNextRing = findConnectedParticles(particlesByWeight, locationsInLastRing);
            List<LocationInThought> nextRingLocations = createNextRing(radialStructure, connectionsForNextRing, locationsInLastRing);
            particlesByWeight.removeAll(connectionsForNextRing);

            List<ThoughtParticle> connectionsWithinNewRing = findParticlesCompletelyWithinRing(particlesByWeight, nextRingLocations);
            createMoreLinksInHighestRing(radialStructure, connectionsWithinNewRing);
            particlesByWeight.removeAll(connectionsWithinNewRing);

            locationsInLastRing = nextRingLocations;

            if (particlesByWeight.size() == lastParticleCount) {
                break;
            } else {
                lastParticleCount = particlesByWeight.size();
            }
        }

        return radialStructure;

    }

    private List<LocationInThought> createNextRing(RadialStructure radialStructure, List<ThoughtParticle> connectionsForNextRing, List<LocationInThought> locationsInLastRing) {
        radialStructure.createNextRing();

        for (ThoughtParticle connectedParticle : connectionsForNextRing) {
            Link link = connectedParticle.getLink();
            LocationInThought locationA = link.getLocationA();
            LocationInThought locationB = link.getLocationB();

            LocationInThought locationToLinkTo = getSourceLocation(locationsInLastRing, locationA, locationB);
            LocationInThought locationToAdd = getConnectedLocation(locationsInLastRing, locationA, locationB);

            radialStructure.addLocationToHighestRing(locationToLinkTo, locationToAdd, link.getTraversalCount(), connectedParticle.getFocusWeight());
        }

        return radialStructure.getLocationsInFirstRing();
    }



    private void createMoreLinksInHighestRing(RadialStructure radialStructure, List<ThoughtParticle> connectionsWithinHighestRing) {
        for (ThoughtParticle particle : connectionsWithinHighestRing) {
            Link link = particle.getLink();
            LocationInThought locationA = link.getLocationA();
            LocationInThought locationB = link.getLocationB();

            radialStructure.addExtraLinkWithinHighestRing(locationA, locationB, link.getTraversalCount(), particle.getFocusWeight());
        }
    }

    private void createMoreLinksInFirstRing(RadialStructure radialStructure, List<ThoughtParticle> connectionsWithinFirstRing) {
        for (ThoughtParticle particle : connectionsWithinFirstRing) {
            Link link = particle.getLink();
            LocationInThought locationA = link.getLocationA();
            LocationInThought locationB = link.getLocationB();

            radialStructure.addExtraLinkWithinFirstRing(locationA, locationB, link.getTraversalCount(), particle.getFocusWeight());
        }
    }


    private List<LocationInThought> createFirstRing(RadialStructure radialStructure, List<ThoughtParticle> firstRingParticles, LocationInThought centerOfFocus) {
        for (ThoughtParticle connectedParticle : firstRingParticles) {
            Link link = connectedParticle.getLink();
            LocationInThought locationA = link.getLocationA();
            LocationInThought locationB = link.getLocationB();

            LocationInThought locationToAdd = getConnectedLocation(centerOfFocus, locationA, locationB);

            radialStructure.addLocationToFirstRing(locationToAdd, link.getTraversalCount(), connectedParticle.getFocusWeight());
        }

        return radialStructure.getLocationsInFirstRing();
    }

    private List<ThoughtParticle> findParticlesCompletelyWithinRing(List<ThoughtParticle> particlesByWeight, List<LocationInThought> firstRingLocations) {
        List<ThoughtParticle> particlesInsideRing = new ArrayList<>();

        for (ThoughtParticle particle : particlesByWeight) {
            Link link = particle.getLink();
            LocationInThought locationA = link.getLocationA();
            LocationInThought locationB = link.getLocationB();

            if (firstRingLocations.contains(locationA) && firstRingLocations.contains(locationB)) {
                particlesInsideRing.add(particle);
            }
        }

        return particlesInsideRing;
    }

    private LocationInThought getSourceLocation(List<LocationInThought> locationsInLastRing, LocationInThought locationA, LocationInThought locationB) {
        if (locationsInLastRing.contains(locationA)) {
            return locationA;
        } else {
            return locationB;
        }

    }

    private LocationInThought getConnectedLocation(List<LocationInThought> locationsInLastRing, LocationInThought locationA, LocationInThought locationB) {
        if (locationsInLastRing.contains(locationA)) {
            return locationB;
        } else {
            return locationA;
        }
    }

    private LocationInThought getConnectedLocation(LocationInThought centerOfFocus, LocationInThought locationA, LocationInThought locationB) {
        if (centerOfFocus == locationA) {
            return locationB;
        } else {
            return locationA;
        }
    }

    private List<ThoughtParticle> findConnectedParticles(List<ThoughtParticle> particlesByWeight, List<LocationInThought> lastRingLocations) {
        List<ThoughtParticle> connectedParticles = new ArrayList<>();

        for (ThoughtParticle particle : particlesByWeight) {
            LocationInThought locationA = particle.getLink().getLocationA();
            LocationInThought locationB = particle.getLink().getLocationB();

            if (lastRingLocations.contains(locationA) || lastRingLocations.contains(locationB)) {
                connectedParticles.add(particle);
            }
        }

        return connectedParticles;
    }

    private List<ThoughtParticle> findConnectedParticles(List<ThoughtParticle> particlesByWeight, LocationInThought centerOfFocus) {
        List<ThoughtParticle> connectedParticles = new ArrayList<>();

        for (ThoughtParticle particle : particlesByWeight) {
            LocationInThought locationA = particle.getLink().getLocationA();
            LocationInThought locationB = particle.getLink().getLocationB();

            if (centerOfFocus == locationA || centerOfFocus == locationB) {
                connectedParticles.add(particle);
            }
        }

        return connectedParticles;
    }

    private LocationInThought findOrCreateLocation(String locationPath) {
        LocationInThought location = locationMap.get(locationPath);
        if (location == null) {
            location = new LocationInThought(this.focalPoint, locationPath, locationIndex++);
            locationMap.put(locationPath, location);
        }
        return location;
    }

    private LocationInThought getCenterOfFocus(List<ThoughtParticle> particlesByWeight) {

        LocationInThought center = null;

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


    public List<ThoughtParticle> getNormalizedParticlesSortedByWeight() {
        List<ThoughtParticle> particles = new ArrayList<>(thoughtParticleMap.values());

        Collections.sort(particles);

        double maxWeight = getMaxWeight(particles);
        double minWeight = getMinWeight(particles);

        for (ThoughtParticle particle : particles) {
            particle.normalize(minWeight, maxWeight);
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


    private ThoughtParticle findOrCreateParticle(Link edge) {

        String particleKey = edge.toKey();
        ThoughtParticle particle = thoughtParticleMap.get(particleKey);

        if (particle == null) {
            particle = new ThoughtParticle(edge);
            thoughtParticleMap.put(particleKey, particle);
        }
        return particle;
    }

    public LocationInThought getCurrentLocation() {
        return currentLocation;
    }


    private class DecayingGrowthRate {
        double growthRate = 1;
        int risingDenominator = 1;

        List<Double> thoughtSpeedRatios = new ArrayList<>();
        int activeThoughtIndex = 0;

        DecayingGrowthRate(List<ThoughtParticle> thoughtTracer) {
            ThoughtParticle mainThought = thoughtTracer.get(0);

            if (thoughtTracer.size() > 1) {
                for (int i = 1; i < thoughtTracer.size(); i++) {
                    ThoughtParticle previousThought = thoughtTracer.get(1);

                    double thoughtSpeedRatio = 1;

                    if (mainThought.getSpeedOfThought() > 0 ) {
                        thoughtSpeedRatio = previousThought.getSpeedOfThought() / mainThought.getSpeedOfThought();
                    }

                    thoughtSpeedRatios.add(thoughtSpeedRatio);
                }
            }

        }

        void decay() {
            activeThoughtIndex++;
            risingDenominator++;

            double activeSpeedRatio = 1;

            if (thoughtSpeedRatios.size() > activeThoughtIndex) {
                activeSpeedRatio = thoughtSpeedRatios.get(activeThoughtIndex);
            }

            this.growthRate = activeSpeedRatio * growthRate / risingDenominator;
        }

        double getRate() {
            return growthRate;
        }
    }

   private class ThoughtParticle implements Comparable<ThoughtParticle> {
       private final Link link;
       private double weight;
       private double normalizedWeight;
       private double speedOfThought;

       ThoughtParticle(Link link) {
           this.link = link;
           this.weight = 0;
       }

       void setSpeedOfThought(Duration timeInLocation) {
           this.speedOfThought = Math.sqrt(timeInLocation.getSeconds());
       }

       void growHeavyWithFocus(double growthFactor, Duration timeInLocation) {
           this.weight += Math.floor( Math.sqrt(timeInLocation.getSeconds()) * growthFactor);
       }

       void normalize(double minWeight, double maxWeight) {
           if ((maxWeight - minWeight) > 0) {
               normalizedWeight = (this.weight - minWeight) / (maxWeight - minWeight);
           }
       }

       public double getFocusWeight() {
           if (normalizedWeight > 0) {
               return normalizedWeight;
           }
           return weight;
       }

       @Override
       public int compareTo(ThoughtParticle o) {
           return Double.compare(weight, o.weight)* -1;
       }

       public Link getLink() {
           return link;
       }

       public double getSpeedOfThought() {
           return speedOfThought;
       }
   }


    private Link findOrCreateEdge(LocationInThought locationA, LocationInThought locationB) {

        String linkKey = createLinkKeyIgnoringOrder(locationA, locationB);

        Link link = linkMap.get(linkKey);

        if (link == null) {
            link = new Link(locationA, locationB);
            linkMap.put(linkKey, link);
        }

        return link;
    }

    private String createLinkKeyIgnoringOrder(LocationInThought locationA, LocationInThought locationB) {
        String pathA = locationA.toKey();
        String pathB = locationB.toKey();

        if (pathA.compareTo(pathB) > 0) {
            pathA = locationB.getLocationPath();
            pathB = locationA.getLocationPath();
        }

        return pathA + pathB;
    }


    private class Link {

        private final LocationInThought locationA;
        private final LocationInThought locationB;
        private int visitCounter;

        Link(LocationInThought locationA, LocationInThought locationB) {
            this.locationA = locationA;
            this.locationB = locationB;
            this.visitCounter = 0;
        }

        LocationInThought getLocationA() {
            return locationA;
        }

        LocationInThought getLocationB() {
            return locationB;
        }

        void visit() {
            this.visitCounter++;
        }

        public String toKey() {
            return locationA.toKey() + "=>" + locationB.toKey();
        }

        public int getTraversalCount() {
            return visitCounter;
        }
    }

}
