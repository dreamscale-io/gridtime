package com.dreamscale.gridtime.core.machine.memory.grid.trackset;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.FeatureRowKey;
import com.dreamscale.gridtime.core.machine.memory.tag.types.*;
import com.dreamscale.gridtime.core.machine.memory.tag.FeatureTag;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.ExecutionEventReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.gridtime.core.machine.memory.grid.track.MetricsTrack;
import com.dreamscale.gridtime.core.machine.memory.grid.track.PlayableCompositeTrackSet;
import com.dreamscale.gridtime.core.machine.memory.grid.track.RhythmMusicTrack;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExecutionTrackSet implements PlayableCompositeTrackSet {

    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;

    private final TrackSetKey trackSetName;

    private final RhythmMusicTrack<ExecutionEventReference> rhythmTrack;
    private final MetricsTrack metricsTrack;


    private boolean isRedAndWantingGreen = false;

    private ExecutionEventReference carryOverLastExec;

    public ExecutionTrackSet(TrackSetKey trackSetName, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.gridTime = gridTime;
        this.musicClock = musicClock;

        this.rhythmTrack = new RhythmMusicTrack<>(FeatureRowKey.EXEC_RHYTHM, gridTime, musicClock);
        this.metricsTrack = new MetricsTrack(gridTime, musicClock);
    }

    public void executeThing(LocalDateTime moment, ExecutionEventReference executionEvent) {

        RelativeBeat beat = musicClock.getClosestBeat(gridTime.getRelativeTime(moment));

        ExecutionEventReference lastEvent = getLatestEventOnOrBeforeBeat(beat);

        rhythmTrack.playEventAtBeat(moment, executionEvent);
        metricsTrack.getMetricsFor(beat).addExecutionTimeSample(executionEvent.getExecutionTime());

        if (lastEvent != null) {
            Duration durationSinceLastExec = Duration.between(lastEvent.getPosition(), executionEvent.getPosition());
            metricsTrack.getMetricsFor(beat).addExecutionCycleTimeSample(durationSinceLastExec);
        }

        if (!isRedAndWantingGreen && executionEvent.isRed()) {
            rhythmTrack.addTagAtBeat(beat, new FeatureTag<>(moment, executionEvent, StartTypeTag.FirstRed));
            isRedAndWantingGreen = true;
        } else if (isRedAndWantingGreen && executionEvent.isGreen()) {
            rhythmTrack.addTagAtBeat(beat, new FeatureTag<>(moment, executionEvent, FinishTypeTag.FirstGreenAfterRed));
            isRedAndWantingGreen = false;
        }

    }

    private ExecutionEventReference getLatestEventOnOrBeforeBeat(RelativeBeat beat) {
        ExecutionEventReference lastExecution = rhythmTrack.findLatestEventOnOrBeforeBeat(beat);

        if (lastExecution == null) {
            lastExecution = carryOverLastExec;
        }

        return lastExecution;
    }


    public CarryOverContext getCarryOverContext(String subcontextName) {

        CarryOverContext carryOverContext = new CarryOverContext(subcontextName);

        ExecutionEventReference lastExecution = rhythmTrack.getLast();
        carryOverContext.saveReference("last.exec", lastExecution);

        carryOverContext.saveStateFlag("is.red.and.wanting.green", isRedAndWantingGreen);

        return carryOverContext;
    }


    public void initFromCarryOverContext(CarryOverContext subContext) {

        carryOverLastExec = subContext.getReference("last.exec");

        Boolean isRedAndWantingGreenFlag = subContext.getStateFlag("is.red.and.wanting.green");
        if (isRedAndWantingGreenFlag != null) {
            isRedAndWantingGreen = isRedAndWantingGreenFlag;
        }
    }

    @Override
    public void populateBoxWithBeat(RelativeBeat beat, GridMetrics boxMetrics) {

    }

    public TrackSetKey getTrackSetKey() {
        return trackSetName;
    }

    @Override
    public void finish() {

    }

    @Override
    public List<GridRow> toGridRows() {
        List<GridRow> rows = new ArrayList<>();

        rows.add(rhythmTrack.toGridRow());

        rows.add(metricsTrack.toGridRow(MetricRowKey.EXECUTION_RUN_TIME, AggregateType.AVG));
        rows.add(metricsTrack.toGridRow(MetricRowKey.EXECUTION_CYCLE_TIME, AggregateType.AVG));

        return rows;
    }

    public Set<? extends FeatureReference> getFeatures() {
        return rhythmTrack.getFeatures();
    }

}
