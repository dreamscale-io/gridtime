package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.executor.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.StartTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.StartTypeTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.FeatureType;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.BandedMusicTrack;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.PlayableCompositeTrack;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.TrackSetName;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class MusicTrackSet<T extends FeatureType, F extends FeatureReference> implements PlayableCompositeTrack {

    private final TrackSetName trackSetName;
    private final MusicClock musicClock;
    private Map<T, BandedMusicTrack<F>> musicTracks = DefaultCollections.map();

    public MusicTrackSet(TrackSetName trackSetName, MusicClock musicClock) {
        this.trackSetName = trackSetName;
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

    public F getFeatureAtBeat(T type, RelativeBeat beat) {
        BandedMusicTrack<F> bandedMusicTrack = musicTracks.get(type);
        if (bandedMusicTrack != null) {
            return bandedMusicTrack.getFeatureAt(beat);
        }

        return null;
    }

    public void startPlaying(T type, RelativeBeat beat, F reference) {
        BandedMusicTrack<F> bandedMusicTrack = findOrCreateTrack(type);

        bandedMusicTrack.startPlaying(beat, reference);
    }

    public void startPlaying(T type, RelativeBeat beat, F reference, StartTag startTag) {
        BandedMusicTrack<F> bandedMusicTrack = findOrCreateTrack(type);

        bandedMusicTrack.startPlaying(beat, reference, startTag);
    }

    public void stopPlaying(T type, RelativeBeat beat, FinishTag finishTag) {
        BandedMusicTrack<F> bandedMusicTrack = findOrCreateTrack(type);
        bandedMusicTrack.stopPlaying(beat, finishTag);
    }

    public void stopPlaying(T type, RelativeBeat beat) {
        BandedMusicTrack<F> bandedMusicTrack = findOrCreateTrack(type);
        bandedMusicTrack.stopPlaying(beat);
    }

    public void clearAllTracks(RelativeBeat beat) {
        for (BandedMusicTrack<F> track : musicTracks.values()) {
            track.stopPlaying(beat);
        }
    }

    private BandedMusicTrack<F> findOrCreateTrack(T type) {
        BandedMusicTrack<F> bandedMusicTrack = musicTracks.get(type);
        if (bandedMusicTrack == null) {
            bandedMusicTrack = new BandedMusicTrack<>(type.toDisplayString(), musicClock);
            musicTracks.put(type, bandedMusicTrack);
            log.debug("Adding track for type: "+type);
        }
        return bandedMusicTrack;
    }

    public void initFirst(T type, F reference) {
        startPlaying(type, musicClock.getStartBeat(), reference, StartTypeTag.Rollover);
    }

    public void finish() {
        for (BandedMusicTrack track : musicTracks.values()) {
            track.finish();
        }
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
