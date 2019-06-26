package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.StartTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.types.StartTypeTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.type.FeatureType;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.BandedMusicTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.PlayableCompositeTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.TrackSetName;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public abstract class MusicTrackSet<T extends FeatureType, F extends FeatureReference> implements PlayableCompositeTrack {

    private final TrackSetName trackSetName;
    private final MusicClock musicClock;
    private final GeometryClock.GridTime gridTime;
    private Map<T, BandedMusicTrack<F>> musicTracks = DefaultCollections.map();

    public MusicTrackSet(TrackSetName trackSetName, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.gridTime = gridTime;
        this.musicClock = musicClock;
    }

    public F getFirst(T type) {
        BandedMusicTrack<F> bandedMusicTrack = musicTracks.get(type);
        if (bandedMusicTrack != null) {
            return bandedMusicTrack.getFirst();
        }
        return null;
    }

    public F getLast(T type) {
        BandedMusicTrack<F> bandedMusicTrack = musicTracks.get(type);
        if (bandedMusicTrack != null) {
            return bandedMusicTrack.getLast();
        }
        return null;
    }

    public F getLastOnOrBeforeMoment(LocalDateTime moment, T type) {
        RelativeBeat beat = musicClock.getClosestBeat(gridTime.getRelativeTime(moment));

        BandedMusicTrack<F> bandedMusicTrack = musicTracks.get(type);
        if (bandedMusicTrack != null) {
            return bandedMusicTrack.getLastOnOrBeforeBeat(beat);
        }
        return null;
    }

    public F getFeatureAtBeat(T type, RelativeBeat beat) {
        BandedMusicTrack<F> bandedMusicTrack = musicTracks.get(type);
        if (bandedMusicTrack != null) {
            return bandedMusicTrack.getFeatureAt(beat);
        }

        return null;
    }

    public void startPlaying(T type, LocalDateTime moment, F reference) {
        BandedMusicTrack<F> bandedMusicTrack = findOrCreateTrack(type);

        bandedMusicTrack.startPlaying(moment, reference);
    }

    public void startPlaying(T type, LocalDateTime moment, F reference, StartTag startTag) {
        BandedMusicTrack<F> bandedMusicTrack = findOrCreateTrack(type);

        bandedMusicTrack.startPlaying(moment, reference, startTag);
    }

    public void stopPlaying(T type, LocalDateTime moment, FinishTag finishTag) {
        BandedMusicTrack<F> bandedMusicTrack = findOrCreateTrack(type);
        bandedMusicTrack.stopPlaying(moment, finishTag);
    }

    public void stopPlaying(T type, LocalDateTime moment) {
        BandedMusicTrack<F> bandedMusicTrack = findOrCreateTrack(type);
        bandedMusicTrack.stopPlaying(moment);
    }

    public void clearAllTracks(LocalDateTime moment,FinishTag finishTag) {
        for (BandedMusicTrack<F> track : musicTracks.values()) {
            track.stopPlaying(moment, finishTag);
        }
    }

    private BandedMusicTrack<F> findOrCreateTrack(T type) {
        BandedMusicTrack<F> bandedMusicTrack = musicTracks.get(type);
        if (bandedMusicTrack == null) {
            bandedMusicTrack = new BandedMusicTrack<>(type.toDisplayString(), gridTime, musicClock);
            musicTracks.put(type, bandedMusicTrack);
            log.debug("Adding track for type: "+type);
        }
        return bandedMusicTrack;
    }

    public void initFirst(T type, F reference) {
        startPlaying(type, null, reference, StartTypeTag.Rollover);
    }

    public void finish() {
        for (BandedMusicTrack track : musicTracks.values()) {
            track.finish();
        }
    }

    public Set<? extends FeatureReference> getFeatures() {
        Set<FeatureReference> features = DefaultCollections.set();

        for (BandedMusicTrack<F> track : musicTracks.values()) {
            features.addAll(track.getFeatures());
        }

        return features;
    }

    public TrackSetName getTrackSetName() {
        return trackSetName;
    }

    @Override
    public List<GridRow> toGridRows() {
        List<GridRow> rows = new ArrayList<>();
        for (BandedMusicTrack<F> track : musicTracks.values()) {
            rows.add(track.toGridRow());
        }

        return rows;
    }


    protected Set<T> getTrackTypes() {
        return musicTracks.keySet();
    }
}
