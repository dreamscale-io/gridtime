package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.AuthorsReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.BandedMusicTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.PlayableCompositeTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.CarryOverContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


public class AuthorsTrackSet implements PlayableCompositeTrack {

    private final TrackSetName trackSetName;
    private final BandedMusicTrack<AuthorsReference> authorsTrack;

    public AuthorsTrackSet(TrackSetName trackSetName, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.authorsTrack = new BandedMusicTrack<>("@author", gridTime, musicClock);
    }

    public void startAuthors(LocalDateTime moment, AuthorsReference authorsReference) {
        authorsTrack.startPlaying(moment, authorsReference);
    }

    public void clearAuthors(LocalDateTime moment) {
        authorsTrack.stopPlaying(moment);
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
