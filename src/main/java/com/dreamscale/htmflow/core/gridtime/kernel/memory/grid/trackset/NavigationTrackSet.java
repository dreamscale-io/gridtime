package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.PlaceReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.AggregateType;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.MetricType;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.*;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class NavigationTrackSet implements PlayableCompositeTrack {

    private final TrackSetName trackSetName;
    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;

    private final RhythmMusicTrack<PlaceReference> rhythmTrack;
    private final BatchMusicTrack<PlaceReference> batchTrack;
    private final MetricsTrack metricsTrack;


    private Map<PlaceReference, String> shortHandReferences = DefaultCollections.map();
    private char shortHandLetter = 'a';

    private PlaceReference carryOverLastLocation;

    public NavigationTrackSet(TrackSetName trackSetName, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.gridTime = gridTime;
        this.musicClock = musicClock;

        this.rhythmTrack = new RhythmMusicTrack<>("@nav/rhythm", gridTime, musicClock);
        this.batchTrack = new BatchMusicTrack<>("@nav/batch", gridTime, musicClock);
        this.metricsTrack = new MetricsTrack(gridTime, musicClock);
    }

    public void gotoPlace(LocalDateTime moment, PlaceReference placeReference, Duration durationInPlace) {
        mapShortHandReference(placeReference);
        rhythmTrack.playEventAtBeat(moment, placeReference);
        batchTrack.playEventAtBeat(moment, placeReference);

        RelativeBeat beat = musicClock.getClosestBeat(gridTime.getRelativeTime(moment));

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

    public Set<? extends FeatureReference> getFeatures() {
        return batchTrack.getFeatures();
    }
}
