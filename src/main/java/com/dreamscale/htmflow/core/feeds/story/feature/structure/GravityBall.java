package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import java.time.Duration;
import java.util.*;

/**
 * Create a 2D structural model of a FocalPoint by first identifying the "focal center",
 * then orienting positions of elements in 2D space as a radial map, relative to focal center.
 */

public class GravityBall {

    private final Map<String, LocationInPlace> locationMap = new HashMap<>();
    private final Map<String, Edge> edgeMap = new HashMap<>();
    private final FocalPoint place;

    private Map<String, Particle> particleMap = new HashMap<>();
    private LinkedList<Particle> thoughtTracer = new LinkedList<>();

    private LocationInPlace currentLocation;
    private int locationIndex = 1;

    private static final int TRACER_LENGTH = 5;

    public GravityBall(FocalPoint place) {
        this.place = place;
    }

    public LocationInPlace gotoLocationInSpace(String locationPath) {

        LocationInPlace fromLocation = currentLocation;
        LocationInPlace toLocation = findOrCreateLocation(locationPath);

        toLocation.visit();

        addParticleForTraversal(fromLocation, toLocation);

        return toLocation;
    }

    public void growHeavyWithFocus(Duration timeInLocation) {
        currentLocation.spendTime(timeInLocation);

        DecayingGrowthRate decayingGrowth = new DecayingGrowthRate();

        for (Particle particle : thoughtTracer) {
            particle.growHeavyWithFocus(decayingGrowth.getRate(), timeInLocation);

            decayingGrowth.decay();
        }
    }

    private void addParticleForTraversal(LocationInPlace fromLocation, LocationInPlace toLocation) {

        Edge edge = findOrCreateEdge(fromLocation, toLocation);
        edge.visit();

        Particle particle = findOrCreateParticle(edge);

        pushParticleOntoThoughtTracer(particle);
    }

    private void pushParticleOntoThoughtTracer(Particle particle) {
        thoughtTracer.push(particle);

        if (thoughtTracer.size() > TRACER_LENGTH) {
            thoughtTracer.removeLast();
        }
    }

    RadialStructure buildRadialStructure() {
        RadialStructure radialStructure = new RadialStructure();

        List<Particle> particlesByWeight = getNormalizedParticlesSortedByWeight();

        LocationInPlace centerOfFocus = getCenterOfFocus(particlesByWeight);
        radialStructure.placeCenter(centerOfFocus);

        List<Particle> firstRingParticles = findConnectedParticles(particlesByWeight, centerOfFocus);
        List<LocationInPlace> firstRingLocations = createFirstRing(radialStructure, firstRingParticles, centerOfFocus);
        particlesByWeight.removeAll(firstRingParticles);

        List<Particle> connectionsWithinFirstRing = findParticlesCompletelyWithinRing(particlesByWeight, firstRingLocations);
        createMoreLinksInFirstRing(radialStructure, connectionsWithinFirstRing);
        particlesByWeight.removeAll(connectionsWithinFirstRing);

        //TODO add outer ring structures

        return radialStructure;

    }

    private void createMoreLinksInFirstRing(RadialStructure radialStructure, List<Particle> connectionsWithinFirstRing) {
        for (Particle particle : connectionsWithinFirstRing) {
            Edge edge = particle.getEdge();
            LocationInPlace locationA = edge.getLocationA();
            LocationInPlace locationB = edge.getLocationB();

            radialStructure.addExtraLinkWithinFirstRing(locationA, locationB, edge.getTraversalCount(), particle.getFocusWeight());
        }
    }


    private List<LocationInPlace> createFirstRing(RadialStructure radialStructure, List<Particle> firstRingParticles, LocationInPlace centerOfFocus) {
        for (Particle connectedParticle : firstRingParticles) {
            Edge edge = connectedParticle.getEdge();
            LocationInPlace locationA = edge.getLocationA();
            LocationInPlace locationB = edge.getLocationB();

            LocationInPlace locationToAdd = getConnectedLocation(centerOfFocus, locationA, locationB);

            radialStructure.addLocationToFirstRing(locationToAdd, edge.getTraversalCount(), connectedParticle.getFocusWeight());
        }

        return radialStructure.getLocationsInFirstRing();
    }

    private List<Particle> findParticlesCompletelyWithinRing(List<Particle> particlesByWeight, List<LocationInPlace> firstRingLocations) {
        List<Particle> particlesInsideRing = new ArrayList<>();

        for (Particle particle : particlesByWeight) {
            Edge edge = particle.getEdge();
            LocationInPlace locationA = edge.getLocationA();
            LocationInPlace locationB = edge.getLocationB();

            if (firstRingLocations.contains(locationA) && firstRingLocations.contains(locationB)) {
                particlesInsideRing.add(particle);
            }
        }

        return particlesInsideRing;
    }



    private LocationInPlace getConnectedLocation(LocationInPlace centerOfFocus, LocationInPlace locationA, LocationInPlace locationB) {
        if (centerOfFocus == locationA) {
            return locationB;
        } else {
            return locationA;
        }
    }

    private List<Particle> findConnectedParticles(List<Particle> particlesByWeight, LocationInPlace centerOfFocus) {
        List<Particle> connectedParticles = new ArrayList<>();

        for (Particle particle : particlesByWeight) {
            LocationInPlace locationA = particle.getEdge().getLocationA();
            LocationInPlace locationB = particle.getEdge().getLocationB();

            if (centerOfFocus == locationA || centerOfFocus == locationB) {
                connectedParticles.add(particle);
            }
        }

        return connectedParticles;
    }

    private LocationInPlace findOrCreateLocation(String locationPath) {
        LocationInPlace location = locationMap.get(locationPath);
        if (location == null) {
            location = new LocationInPlace(this.place, locationPath, locationIndex++);
            locationMap.put(locationPath, location);
        }
        return location;
    }

    private LocationInPlace getCenterOfFocus(List<Particle> particlesByWeight) {

        LocationInPlace center = null;

        if (particlesByWeight.size() > 0) {
            Particle heaviest = particlesByWeight.get(0);

            Edge edge = heaviest.getEdge();

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



    public List<Particle> getNormalizedParticlesSortedByWeight() {
        List<Particle> particles = new ArrayList<>(particleMap.values());

        Collections.sort(particles);

        double maxWeight = getMaxWeight(particles);
        double minWeight = getMinWeight(particles);

        for (Particle particle : particles) {
            particle.normalize(minWeight, maxWeight);
        }

        return particles;
    }

    private double getMaxWeight(List<Particle> particles) {
        double max = 1;
        if (particles.size() > 0) {
            max = particles.get(0).getFocusWeight();
        }
        return max;
    }

    private double getMinWeight(List<Particle> particles) {
        double min = 0;
        if (particles.size() > 1) {
            min = particles.get(particles.size() - 1).getFocusWeight();
        }
        return min;
    }


    private Particle findOrCreateParticle(Edge edge) {

        String particleKey = edge.toKey();
        Particle particle = particleMap.get(particleKey);

        if (particle == null) {
            particle = new Particle(edge);
            particleMap.put(particleKey, particle);
        }
        return particle;
    }

    public LocationInPlace getCurrentLocation() {
        return currentLocation;
    }


    private class DecayingGrowthRate {
        double growthRate = 1;

        void decay() {
            this.growthRate = growthRate / 2;
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

   private class Particle implements Comparable<Particle> {
       private final Edge edge;
       private double weight;
       private double normalizedWeight;

       Particle(Edge edge) {
           this.edge = edge;
           this.weight = 0;
       }

       void growHeavyWithFocus(double growthFactor, Duration timeInLocation) {
           this.weight += Math.floor( timeInLocation.getSeconds() * growthFactor);
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
       public int compareTo(Particle o) {
           return Double.compare(weight, o.weight)* -1;
       }

       public Edge getEdge() {
           return edge;
       }
   }


    private Edge findOrCreateEdge(LocationInPlace locationA, LocationInPlace locationB) {

        String edgeKey = createKeyIgnoringOrder(locationA, locationB);

        Edge edge = edgeMap.get(edgeKey);

        if (edge == null) {
            edge = new Edge(locationA, locationB);
            edgeMap.put(edgeKey, edge);
        }

        return edge;
    }

    private String createKeyIgnoringOrder(LocationInPlace locationA, LocationInPlace locationB) {
        String pathA = locationA.toKey();
        String pathB = locationB.toKey();

        if (pathA.compareTo(pathB) > 0) {
            pathA = locationB.getLocationPath();
            pathB = locationA.getLocationPath();
        }

        return pathA + pathB;
    }


    private class Edge {

        private final LocationInPlace locationA;
        private final LocationInPlace locationB;
        private int visitCounter;

        Edge(LocationInPlace locationA, LocationInPlace locationB) {
            this.locationA = locationA;
            this.locationB = locationB;
            this.visitCounter = 0;
        }

        LocationInPlace getLocationA() {
            return locationA;
        }

        LocationInPlace getLocationB() {
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
