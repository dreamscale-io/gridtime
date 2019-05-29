package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.executor.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.FeatureCell;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridCell;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
