package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeelsReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.BandedMusicTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.PlayableCompositeTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.CarryOverContext;

import java.util.List;
import java.util.Set;


public class FeelsTrackSet implements PlayableCompositeTrack {

    private final TrackSetName trackSetName;
    private final BandedMusicTrack<FeelsReference> feelsTrack;

    public FeelsTrackSet(TrackSetName trackSetName, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.feelsTrack = new BandedMusicTrack<>("@feels", musicClock);
    }

    public void startFeels(RelativeBeat fromBeat, FeelsReference feelsReference) {
        feelsTrack.startPlaying(fromBeat, feelsReference);
    }

    public void clearFeels(RelativeBeat toBeat) {
        feelsTrack.stopPlaying(toBeat);
    }

    public void finish() {
        feelsTrack.finish();
    }

    @Override
    public TrackSetName getTrackSetName() {
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