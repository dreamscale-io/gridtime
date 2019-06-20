package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.executor.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.PlaceReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.AggregateType;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.MetricType;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.*;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class NavigationTrackSet implements PlayableCompositeTrack {

    private final TrackSetName trackSetName;
    private final MusicClock musicClock;
    private final RhythmMusicTrack<PlaceReference> rhythmTrack;
    private final BatchMusicTrack<PlaceReference> batchTrack;
    private final MetricsTrack metricsTrack;


    private Map<PlaceReference, String> shortHandReferences = DefaultCollections.map();
    private char shortHandLetter = 'a';

    private PlaceReference carryOverLastLocation;

    public NavigationTrackSet(TrackSetName trackSetName, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.musicClock = musicClock;

        this.rhythmTrack = new RhythmMusicTrack<>("@nav/rhythm", musicClock);
        this.batchTrack = new BatchMusicTrack<>("@nav/batch", musicClock);
        this.metricsTrack = new MetricsTrack(musicClock);
    }

    public void gotoPlace(RelativeBeat beat, PlaceReference placeReference, Duration durationInPlace) {
        mapShortHandReference(placeReference);

        rhythmTrack.playEventAtBeat(beat, placeReference);
        batchTrack.playEventAtBeat(beat, placeReference);

        metricsTrack.getMetricsFor(beat).addVelocitySample(durationInPlace);
    }

    private void mapShortHandReference(PlaceReference placeReference) {
        String shortHand = shortHandReferences.get(placeReference);

        if (shortHand == null) {
            shortHand = String.valueOf(shortHandLetter);
            shortHandReferences.put(placeReference, shortHand);
            shortHandLetter++;
        }
    }

    public void modifyPlace(RelativeBeat beat, int modificationCount) {
        log.debug("modify: "+beat.toDisplayString() + modificationCount);
        metricsTrack.getMetricsFor(beat).addModificationSample(modificationCount);
    }

    public CarryOverContext getCarryOverContext(String subcontextName) {

        CarryOverContext carryOverContext = new CarryOverContext(subcontextName);

        PlaceReference lastLocation = rhythmTrack.getLast();
        carryOverContext.saveReference("last.location", lastLocation);

        return carryOverContext;
    }

    public void initFromCarryOverContext(CarryOverContext subContext) {

        carryOverLastLocation = (PlaceReference) subContext.getReference("last.location");
    }

    public TrackSetName getTrackSetName() {
        return trackSetName;
    }

    @Override
    public List<GridRow> toGridRows() {
        List<GridRow> rows = new ArrayList<>();

        rows.add(rhythmTrack.toGridRow(shortHandReferences));
        rows.add(batchTrack.toGridRow(shortHandReferences));

        rows.add(metricsTrack.toGridRow(MetricType.FILE_TRAVERSAL_VELOCITY, AggregateType.AVG));
        rows.add(metricsTrack.toGridRow(MetricType.MODIFICATION_COUNT, AggregateType.AVG));

        return rows;
    }


}
