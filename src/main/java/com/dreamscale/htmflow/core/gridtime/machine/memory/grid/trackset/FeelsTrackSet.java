package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeelsReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.FeatureRowKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track.BandedMusicTrack;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track.PlayableCompositeTrackSet;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


public class FeelsTrackSet implements PlayableCompositeTrackSet {

    private final TrackSetKey trackSetName;
    private final BandedMusicTrack<FeelsReference> feelsTrack;

    public FeelsTrackSet(TrackSetKey trackSetName, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.feelsTrack = new BandedMusicTrack<>(FeatureRowKey.FEELS_RATING, gridTime, musicClock);
    }

    public void startFeels(LocalDateTime moment, FeelsReference feelsReference) {
        feelsTrack.startPlaying(moment, feelsReference);
    }

    public void clearFeels(LocalDateTime moment) {
        feelsTrack.stopPlaying(moment);
    }

    public void finish() {
        feelsTrack.finish();
    }

    @Override
    public TrackSetKey getTrackSetKey() {
        return trackSetName;
    }

    @Override
    public List<GridRow> toGridRows() {
        return DefaultCollections.toList(feelsTrack.toGridRow());
    }

    public CarryOverContext getCarryOverContext(String subcontextName) {

        CarryOverContext carryOverContext = new CarryOverContext(subcontextName);

        FeelsReference lastState = feelsTrack.getLast();
        carryOverContext.saveReference("last.feels", lastState);

        return carryOverContext;
    }

    public void initFromCarryOverContext(CarryOverContext subContext) {
        FeelsReference lastState = (FeelsReference) subContext.getReference("last.feels");
        feelsTrack.initFirst(lastState);
    }

    public Set<? extends FeatureReference> getFeatures() {
        return feelsTrack.getFeatures();
    }
}