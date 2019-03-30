package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import java.time.Duration;
import java.util.*;

/**
 * Create a 2D structural model of a FocalPoint by first identifying the "focal center",
 * then orienting positions of elements in 2D space as a radial map, relative to focal center.
 */

public class GravityBallOfThoughts {

    private final Map<String, LocationInThought> locationMap = new HashMap<>();
    private final Map<String, Link> linkMap = new HashMap<>();
    private final FocalPoint place;

    private Map<String, ThoughtParticle> particleMap = new HashMap<>();
    private LinkedList<ThoughtParticle> thoughtTracer = new LinkedList<>();

    private LocationInThought currentLocation;
    private int locationIndex = 1;

    private static final int TRACER_LENGTH = 5;

    public GravityBallOfThoughts(FocalPoint place) {
        this.place = place;
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

        //TODO add outer ring structures

        return radialStructure;

    }

    private void createMoreLinksInFirstRing(RadialStructure radialStructure, List<ThoughtParticle> connectionsWithinFirstRing) {
        for (ThoughtParticle particle : connectionsWithinFirstRing) {
            Link edge = particle.getEdge();
            LocationInThought locationA = edge.getLocationA();
            LocationInThought locationB = edge.getLocationB();

            radialStructure.addExtraLinkWithinFirstRing(locationA, locationB, edge.getTraversalCount(), particle.getFocusWeight());
        }
    }


    private List<LocationInThought> createFirstRing(RadialStructure radialStructure, List<ThoughtParticle> firstRingParticles, LocationInThought centerOfFocus) {
        for (ThoughtParticle connectedParticle : firstRingParticles) {
            Link edge = connectedParticle.getEdge();
            LocationInThought locationA = edge.getLocationA();
            LocationInThought locationB = edge.getLocationB();

            LocationInThought locationToAdd = getConnectedLocation(centerOfFocus, locationA, locationB);

            radialStructure.addLocationToFirstRing(locationToAdd, edge.getTraversalCount(), connectedParticle.getFocusWeight());
        }

        return radialStructure.getLocationsInFirstRing();
    }

    private List<ThoughtParticle> findParticlesCompletelyWithinRing(List<ThoughtParticle> particlesByWeight, List<LocationInThought> firstRingLocations) {
        List<ThoughtParticle> particlesInsideRing = new ArrayList<>();

        for (ThoughtParticle particle : particlesByWeight) {
            Link edge = particle.getEdge();
            LocationInThought locationA = edge.getLocationA();
            LocationInThought locationB = edge.getLocationB();

            if (firstRingLocations.contains(locationA) && firstRingLocations.contains(locationB)) {
                particlesInsideRing.add(particle);
            }
        }

        return particlesInsideRing;
    }



    private LocationInThought getConnectedLocation(LocationInThought centerOfFocus, LocationInThought locationA, LocationInThought locationB) {
        if (centerOfFocus == locationA) {
            return locationB;
        } else {
            return locationA;
        }
    }

    private List<ThoughtParticle> findConnectedParticles(List<ThoughtParticle> particlesByWeight, LocationInThought centerOfFocus) {
        List<ThoughtParticle> connectedParticles = new ArrayList<>();

        for (ThoughtParticle particle : particlesByWeight) {
            LocationInThought locationA = particle.getEdge().getLocationA();
            LocationInThought locationB = particle.getEdge().getLocationB();

            if (centerOfFocus == locationA || centerOfFocus == locationB) {
                connectedParticles.add(particle);
            }
        }

        return connectedParticles;
    }

    private LocationInThought findOrCreateLocation(String locationPath) {
        LocationInThought location = locationMap.get(locationPath);
        if (location == null) {
            location = new LocationInThought(this.place, locationPath, locationIndex++);
            locationMap.put(locationPath, location);
        }
        return location;
    }

    private LocationInThought getCenterOfFocus(List<ThoughtParticle> particlesByWeight) {

        LocationInThought center = null;

        if (particlesByWeight.size() > 0) {
            ThoughtParticle heaviest = particlesByWeight.get(0);

            Link edge = heaviest.getEdge();

            Duration timeInLocationA = edge.getLocationA().getTimeInPlace();
            Duration timeInLocationB = edge.getLocationB().getTimeInPlace();

            if (timeInLocationA.compareTo(timeInLocationB) > 0) {
                center = edge.getLocationA();
            } else {
                center = edge.getLocationB();
            }
        }

        return center;
    }

    //find center
    //arround the center, calc hop count distance, any hops that connect 2 1hops, are in same ring but hop 2
    //ring 2, is 2 hops out that introduces a new node that isn't within the first ring
    //then we look for connectors that are completely within ring 2, that don't introduce new nodes, 3 hops out

    //within each ring, fan out all the things... divide 360 by number of things
    //then sort the things inside the ring by closest neighbor, so the ordering minimizes distance within ring

    //then ring two, fan out all the things, but position the connectors close to their ring 1 link positions
    //I can do this with musical beats too to construct a radial clock with 360 beats.  Rhythm Rings.

    //Enter and Exit are placed on the sides, and arrows go in/out of all nodes where we jump focus.



    public List<ThoughtParticle> getNormalizedParticlesSortedByWeight() {
        List<ThoughtParticle> particles = new ArrayList<>(particleMap.values());

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
        ThoughtParticle particle = particleMap.get(particleKey);

        if (particle == null) {
            particle = new ThoughtParticle(edge);
            particleMap.put(particleKey, particle);
        }
        return particle;
    }

    public LocationInThought getCurrentLocation() {
        return currentLocation;
    }


    private class DecayingGrowthRate {
        double growthRate = 1;

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
            double activeSpeedRatio = 1;

            if (thoughtSpeedRatios.size() > activeThoughtIndex) {
                activeSpeedRatio = thoughtSpeedRatios.get(activeThoughtIndex);
            }

            this.growthRate = activeSpeedRatio * growthRate / 2;
        }

        double getRate() {
            return growthRate;
        }
    }

    private class Traversal {
       private final String from;
       private final String to;
        private final String key;

        Traversal(String from, String to) {
           this.from = from;
           this.to = to;
           this.key =  from + "=>"+to;
       }

        public String getKey() {
            return key;
        }
    }

   private class ThoughtParticle implements Comparable<ThoughtParticle> {
       private final Link edge;
       private double weight;
       private double normalizedWeight;
       private double speedOfThought;

       ThoughtParticle(Link edge) {
           this.edge = edge;
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

       public Link getEdge() {
           return edge;
       }

       public double getSpeedOfThought() {
           return speedOfThought;
       }
   }


    private Link findOrCreateEdge(LocationInThought locationA, LocationInThought locationB) {

        String edgeKey = createKeyIgnoringOrder(locationA, locationB);

        Link edge = linkMap.get(edgeKey);

        if (edge == null) {
            edge = new Link(locationA, locationB);
            linkMap.put(edgeKey, edge);
        }

        return edge;
    }

    private String createKeyIgnoringOrder(LocationInThought locationA, LocationInThought locationB) {
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
