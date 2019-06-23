package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.executor.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.AuthorsReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeelsReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.BandedMusicTrack;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.PlayableCompositeTrack;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.CarryOverContext;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public class AuthorsTrackSet implements PlayableCompositeTrack {

    private final TrackSetName trackSetName;
    private final BandedMusicTrack<AuthorsReference> authorsTrack;

    public AuthorsTrackSet(TrackSetName trackSetName, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.authorsTrack = new BandedMusicTrack<>("@author", musicClock);
    }

    public void startAuthors(RelativeBeat fromBeat, AuthorsReference authorsReference) {
        authorsTrack.startPlaying(fromBeat, authorsReference);
    }

    public void clearAuthors(RelativeBeat toBeat) {
        authorsTrack.stopPlaying(toBeat);
    }

    public void finish() {
        authorsTrack.finish();
    }

    @Override
    public TrackSetName getTrackSetName() {
        return trackSetName;
    }

    @Override
    public List<GridRow> toGridRows() {
        return DefaultCollections.toList(authorsTrack.toGridRow());
    }

    public CarryOverContext getCarryOverContext(String subcontextName) {

        CarryOverContext carryOverContext = new CarryOverContext(subcontextName);

        AuthorsReference lastAuthors = authorsTrack.getLast();
        carryOverContext.saveReference("last.authors", lastAuthors);

        return carryOverContext;
    }

    public void initFromCarryOverContext(CarryOverContext subContext) {
        AuthorsReference lastAuthors = (AuthorsReference) subContext.getReference("last.authors");
        authorsTrack.initFirst(lastAuthors);
    }

    public Set<? extends FeatureReference> getFeatures() {
        return authorsTrack.getFeatures();
    }
}
