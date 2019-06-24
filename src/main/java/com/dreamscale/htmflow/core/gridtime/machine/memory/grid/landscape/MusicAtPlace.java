package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.landscape;

import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.EventDetails;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.PlaceType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridMetrics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MusicAtPlace {

    private final PlaceType placeType;

    private final RelativeBeat[] summaryBeats;
    private final RelativeBeat[] detailBeats;

    private Map<PlaceReference, MusicTrack> placeMusic = new LinkedHashMap<>();

    public MusicAtPlace(MusicClock musicClock, PlaceType placeType ) {
        this.summaryBeats = musicClock.toSummaryClock().getClockBeats();
        this.detailBeats = musicClock.getClockBeats();

        this.placeType = placeType;
    }

    public void playEvent(PlaceReference placeReference, RelativeBeat beat, EventDetails eventDetails) {
        MusicTrack musicTrack = placeMusic.get(placeReference);
        if (musicTrack == null) {
            musicTrack = new MusicTrack(placeReference);
        }

        musicTrack.playEvent(beat, eventDetails);
    }

    public GridMetrics getMetricsFor(PlaceReference placeReference, RelativeBeat beat) {
        MusicTrack musicTrack = placeMusic.get(placeReference);
        if (musicTrack == null) {
            musicTrack = new MusicTrack(placeReference);
        }

        return musicTrack.getMetricsFor(beat);
    }

    private static class MusicTrack {
        PlaceReference place;
        LinkedHashMap<RelativeBeat, GridMetrics> placeMetrics = DefaultCollections.map();
        LinkedHashMap<RelativeBeat, List<EventDetails>> placeEvents = DefaultCollections.map();

        MusicTrack(PlaceReference placeReference) {
            this.place = placeReference;
        }

        public void playEvent(RelativeBeat beat, EventDetails eventDetails) {
            List<EventDetails> events = placeEvents.get(beat);
            if (events == null) {
                events = DefaultCollections.list();
                placeEvents.put(beat, events);
            }

            events.add(eventDetails);
        }

        public GridMetrics getMetricsFor(RelativeBeat beat) {
            GridMetrics metrics = placeMetrics.get(beat);
            if (metrics == null) {
                metrics = new GridMetrics();
                placeMetrics.put(beat, metrics);
            }

            return metrics;
        }
    }

}
