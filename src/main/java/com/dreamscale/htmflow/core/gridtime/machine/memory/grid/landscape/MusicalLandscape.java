package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.landscape;

import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.FeatureDetails;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.FeatureType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridMetrics;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.Map;

public class MusicalLandscape<T extends FeatureType, F extends FeatureReference> {

    private final MusicClock musicClock;
    private final TrackSetName trackSetName;
    private Map<T, MetricsLandscape> landscapesByType = DefaultCollections.map();


    public MusicalLandscape(TrackSetName trackSetName, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.musicClock = musicClock;
    }

    public GridMetrics getMetricsFor(F place, RelativeBeat column) {
        MetricsLandscape metricsLandscape = findOrCreateLandscapeFor(place);

        return metricsLandscape.getMetricsFor(place, column);
    }

    public void addAnnotation(F place, RelativeBeat beat, FeatureDetails annotation) {
        MetricsLandscape metricsLandscape = findOrCreateLandscapeFor(place);

        metricsLandscape.annotateBeat(place, beat, annotation);

    }

    private MetricsLandscape findOrCreateLandscapeFor(F place) {
        MetricsLandscape metricsLandscape = landscapesByType.get(place.getFeatureType());

        if (metricsLandscape == null) {
            metricsLandscape = new MetricsLandscape(musicClock, (T) place.getFeatureType());
        }
        return metricsLandscape;
    }


    private class MetricsLandscape {

        private final MusicClock musicClock;
        private final T placeType; //one layer for boxes, one for locations, one for traversals etc

        LinkedHashMap<F, MetricsTrack> placeMusic = DefaultCollections.map();

        MetricsLandscape(MusicClock musicClock, T placeType) {
            this.musicClock = musicClock;
            this.placeType = placeType;
        }

        public GridMetrics getMetricsFor(F place, RelativeBeat column) {
            MetricsTrack track = findOrCreateTrack(place);

            return track.getMetricsFor(column);
        }

        public void annotateBeat(F place, RelativeBeat beat, FeatureDetails details) {
            MetricsTrack track = findOrCreateTrack((F) place);

            track.annotateBeat(beat, details);
        }

        private MetricsTrack findOrCreateTrack(F place) {
            MetricsTrack track = placeMusic.get(place);

            if (track == null) {
                track = new MetricsTrack(place);
            }
            return track;
        }

    }

    private class MetricsTrack {

        private F placeReference;
        private LinkedHashMap<RelativeBeat, GridMetrics> metricsPerBeat = DefaultCollections.map();
        private MultiValueMap<RelativeBeat, FeatureDetails> annotationsPerBeat = DefaultCollections.multiMap();

        public MetricsTrack(F place) {
            this.placeReference = place;
        }

        public GridMetrics getMetricsFor(RelativeBeat beat) {
            RelativeBeat summaryBeat = beat.toSummaryBeat();

            GridMetrics metrics = metricsPerBeat.get(beat);
            GridMetrics summaryMetrics = metricsPerBeat.get(summaryBeat);

            if (summaryMetrics == null) {
                summaryMetrics = new GridMetrics();
                metricsPerBeat.put(summaryBeat, summaryMetrics);
            }

            if (metrics == null) {
                metrics = new GridMetrics(summaryMetrics);
                metricsPerBeat.put(beat, metrics);
            }
            return metrics;
        }

        public void annotateBeat(RelativeBeat beat, FeatureDetails details) {
            annotationsPerBeat.add(beat, details);
        }

    }

}
