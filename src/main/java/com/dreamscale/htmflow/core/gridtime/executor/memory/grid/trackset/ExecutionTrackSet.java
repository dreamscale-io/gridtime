package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.executor.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.*;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.ExecutionReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.AggregateType;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.MetricType;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.MetricsTrack;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.PlayableCompositeTrack;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.RhythmMusicTrack;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.CarryOverContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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


}
