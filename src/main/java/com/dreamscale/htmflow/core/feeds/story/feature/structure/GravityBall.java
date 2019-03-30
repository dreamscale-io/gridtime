package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import java.time.Duration;
import java.util.*;

public class GravityBall {

    private Map<String, Particle> particleMap = new HashMap<>();
    private LinkedList<Particle> particleThoughtTrail = new LinkedList<>();

    private static final int PARTICLE_TRAIL_SIZE = 5;


    public void createParticleFromTraversal(LocationInPlace fromLocation, LocationInPlace toLocation) {
        Traversal traversal = new Traversal(fromLocation, toLocation);

        Particle particle = findOrCreateParticle(traversal);

        pushParticleOntoThoughtTrail(particle);
    }

    private void pushParticleOntoThoughtTrail(Particle particle) {
        particleThoughtTrail.push(particle);

        if (particleThoughtTrail.size() > PARTICLE_TRAIL_SIZE) {
            particleThoughtTrail.removeLast();
        }
    }

    private List<Particle> getParticlesSortedDescendingByWeight() {
        List<Particle> particles = new ArrayList<>(particleMap.values());

        Collections.sort(particles);

        return particles;
    }


    public void growWithFocus(Duration timeInLocation) {
        DecayingGrowthRate decayingGrowth = new DecayingGrowthRate();

        for (Particle particle : particleThoughtTrail) {
            particle.growWithFocus(decayingGrowth.getRate(), timeInLocation);

            decayingGrowth.decay();
        }
    }



    private Particle findOrCreateParticle(Traversal traversal) {

        Particle particle = particleMap.get(traversal.getKey());

        if (particle == null) {
            particle = new Particle(traversal);
            particleMap.put(traversal.getKey(), particle);
        }
        return particle;
    }


    private class DecayingGrowthRate {
        int denominator = 1;
        double growthRate = 1;

        void decay() {
            denominator++;
            this.growthRate = 1.0 / denominator;
        }

        public double getRate() {
            return growthRate;
        }
    }

    private class Traversal {
       private final LocationInPlace from;
       private final LocationInPlace to;
        private final String key;

        Traversal(LocationInPlace from, LocationInPlace to) {
           this.from = from;
           this.to = to;
           this.key =  from.toKey() + "=>"+to.toKey();
       }

        public String getKey() {
            return key;
        }
    }

   private class Particle implements Comparable<Particle> {
       private final Traversal traversal;
       private float weight;

       Particle(Traversal traversal) {
           this.traversal = traversal;
           this.weight = 0;
       }

       public void growWithFocus(double growthFactor, Duration timeInLocation) {
           this.weight += Math.floor( timeInLocation.getSeconds() * growthFactor);
       }

       float getWeight() {
           return weight;
       }

       @Override
       public int compareTo(Particle o) {
           return Double.compare(weight, o.weight)* -1;
       }
   }


}
