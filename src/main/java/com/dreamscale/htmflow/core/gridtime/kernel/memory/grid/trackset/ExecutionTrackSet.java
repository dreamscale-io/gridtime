package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.types.*;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.ExecutionReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.AggregateType;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.MetricType;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.MetricsTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.PlayableCompositeTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.RhythmMusicTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.CarryOverContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExecutionTrackSet implements PlayableCompositeTrack {

    private final TrackSetName trackSetName;
    private final MusicClock musicClock;
    private final RhythmMusicTrack<ExecutionReference> rhythmTrack;
    private final MetricsTrack metricsTrack;

    private boolean isRedAndWantingGreen = false;

    private ExecutionReference carryOverLastExec;

    public ExecutionTrackSet(TrackSetName trackSetName, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.musicClock = musicClock;

        this.rhythmTrack = new RhythmMusicTrack<>("@exec/rhythm", musicClock);
        this.metricsTrack = new MetricsTrack(musicClock);
    }

    public void executeThing(RelativeBeat beat, ExecutionReference executionEvent) {

        ExecutionReference lastEvent = getLatestEventOnOrBeforeBeat(beat);

        rhythmTrack.playEventAtBeat(beat, executionEvent);
        metricsTrack.getMetricsFor(beat).addExecutionTimeSample(executionEvent.getExecutionTime());

        if (lastEvent != null) {
            Duration durationSinceLastExec = Duration.between(lastEvent.getPosition(), executionEvent.getPosition());
            metricsTrack.getMetricsFor(beat).addExecutionCycleTimeSample(durationSinceLastExec);
        }



        if (!isRedAndWantingGreen && executionEvent.isRed()) {
            rhythmTrack.addTagAtBeat(beat, new FeatureTag<>(executionEvent, StartTypeTag.FirstRed));
            isRedAndWantingGreen = true;
        } else if (isRedAndWantingGreen && executionEvent.isGreen()) {
            rhythmTrack.addTagAtBeat(beat, new FeatureTag<>(executionEvent, FinishTypeTag.FirstGreenAfterRed));
            isRedAndWantingGreen = false;
        }

    }

    private ExecutionReference getLatestEventOnOrBeforeBeat(RelativeBeat beat) {
        ExecutionReference lastExecution = rhythmTrack.getLatestEventOnOrBeforeBeat(beat);

        if (lastExecution == null) {
            lastExecution = carryOverLastExec;
        }

        return lastExecution;
    }


    public CarryOverContext getCarryOverContext(String subcontextName) {

        CarryOverContext carryOverContext = new CarryOverContext(subcontextName);

        ExecutionReference lastExecution = rhythmTrack.getLast();
        carryOverContext.saveReference("last.exec", lastExecution);

        carryOverContext.saveStateFlag("is.red.and.wanting.green", isRedAndWantingGreen);

        return carryOverContext;
    }


    public void initFromCarryOverContext(CarryOverContext subContext) {

        carryOverLastExec = (ExecutionReference) subContext.getReference("last.exec");

        Boolean isRedAndWantingGreenFlag = subContext.getStateFlag("is.red.and.wanting.green");
        if (isRedAndWantingGreenFlag != null) {
            isRedAndWantingGreen = isRedAndWantingGreenFlag;
        }
    }

    public TrackSetName getTrackSetName() {
        return trackSetName;
    }

    @Override
    public List<GridRow> toGridRows() {
        List<GridRow> rows = new ArrayList<>();

        rows.add(rhythmTrack.toGridRow());

        rows.add(metricsTrack.toGridRow(MetricType.EXECUTION_RUN_TIME, AggregateType.AVG));
        rows.add(metricsTrack.toGridRow(MetricType.EXECUTION_CYCLE_TIME, AggregateType.AVG));

        return rows;
    }

    public Set<? extends FeatureReference> getFeatures() {
        return rhythmTrack.getFeatures();
    }

}
