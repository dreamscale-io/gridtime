package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.landscape;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.PlaceReference;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.type.PlaceType;

import java.time.Duration;
import java.util.*;

public class NavigationMetricsGrid extends MusicalLandscape<PlaceType, PlaceReference> {

    private PlaceReference lastLocation;
    private PlaceReference lastTraversal;

    private static final int TRACER_LENGTH = 5;
    private LinkedList<PlaceReference> recentFocusTracer = new LinkedList<>();


    public NavigationMetricsGrid(TrackSetName trackSetName, MusicClock musicClock) {
        super(trackSetName, musicClock);
    }

    public void gotoLocation(FeatureCache featureCache, RelativeBeat beat, PlaceReference newLocation, Duration timeInLocation) {

        getMetricsFor(newLocation, beat).addVelocitySample(timeInLocation);

        if (lastLocation != null) {
            PlaceReference traversal = featureCache.lookupTraversalReference(lastLocation, newLocation);
            getMetricsFor(traversal, beat).addVelocitySample(timeInLocation);
        }

        lastLocation = newLocation;

        //TODO I can focus weight these edges, even if I don't have a way to distinguish between box boundaries yet
    }



    public void modifyCurrentLocation(RelativeBeat beat, int modificationCount) {
        if (lastLocation != null) {
            getMetricsFor(lastLocation, beat).addModificationSample(modificationCount);
        }
        if (lastTraversal != null) {
            getMetricsFor(lastTraversal, beat).addModificationSample(modificationCount);
        }
    }

    private class FocalPoint {
        private PlaceReference box;

        private Set<PlaceReference> locationsInBox = DefaultCollections.set();
        private Set<PlaceReference> traversalsInBox = DefaultCollections.set();
        private Set<PlaceReference> bridgesFromThisBox = DefaultCollections.set();

        FocalPoint(PlaceReference box) {
            this.box = box;
        }

        void addLocationInBox(PlaceReference location) {
            locationsInBox.add(location);
        }

        void addTraversalInBox(PlaceReference traversal) {
            traversalsInBox.add(traversal);
        }

        void addBridgeFromThisBox(PlaceReference bridge) {
            bridgesFromThisBox.add(bridge);
        }
    }

    private class DecayingGrowthRate {
        private final long scaleRelativeToTime;
        double growthRate = 1;
        int risingDenominator = 1;

        DecayingGrowthRate(Duration scaleRelativeToTime) {
            this.scaleRelativeToTime = scaleRelativeToTime.getSeconds();
        }

        void decay() {
            risingDenominator++;

            this.growthRate = 1.0 / risingDenominator;
        }

        double calculateGrowthFor(double timeInSeconds) {
            if (scaleRelativeToTime >= 0) {
                return growthRate * ((1.0 * timeInSeconds) / scaleRelativeToTime);
            } else {
                return 0;
            }
        }

    }



}
