package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.key.FeatureRowKey;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class BatchMusicTrack<F extends FeatureReference> extends RhythmMusicTrack<F> {

    public BatchMusicTrack(FeatureRowKey rowKey, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        super(rowKey, gridTime, musicClock);
    }

    @Override
    public void playEventAtBeat(RelativeBeat beat, F event) {

        List<F> features = getAllFeaturesAtBeat(beat);
        if (features == null || !features.contains(event)) {
            super.playEventAtBeat(beat, event);
        }
    }


    @Override
    public void playEventAtBeat(LocalDateTime moment, F event) {
        RelativeBeat beat = super.getBeat(moment);

        List<F> features = getAllFeaturesAtBeat(beat);
        if (features == null || !features.contains(event)) {
            super.playEventAtBeat(moment, event);
        }
    }

}
