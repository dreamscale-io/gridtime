package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class BatchMusicTrack<F extends FeatureReference> extends RhythmMusicTrack<F> {

    public BatchMusicTrack(String rowName, MusicClock musicClock) {
        super(rowName, musicClock);
    }

    public void playEventAtBeat(RelativeBeat beat, F event) {
        List<F> features = getAllFeaturesAtBeat(beat);
        if (features == null || !features.contains(event)) {
            super.playEventAtBeat(beat, event);
        }
    }

}
