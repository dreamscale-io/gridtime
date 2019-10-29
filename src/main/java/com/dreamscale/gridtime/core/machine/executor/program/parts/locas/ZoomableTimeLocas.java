package com.dreamscale.gridtime.core.machine.executor.program.parts.locas;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.grid.AggregateMetricGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class ZoomableTimeLocas<T> implements Locas {

    private final UUID teamId;
    private final UUID torchieId;
    private final InputStrategy<T> input;
    private final OutputStrategy output;
    private final FeatureCache featureCache;

    private AggregateMetricGrid aggregateMetricGrid;

    public ZoomableTimeLocas(UUID teamId, UUID torchieId, FeatureCache featureCache,
                             InputStrategy<T> input,
                             OutputStrategy output) {
        this.teamId = teamId;
        this.torchieId = torchieId;
        this.featureCache = featureCache;
        this.input = input;
        this.output = output;

    }

    @Override
    public AggregateMetricGrid runProgram(Metronome.TickScope tickScope) {
        List<T> metricInputs = input.breatheIn(teamId, torchieId, tickScope);

        log.debug("Found "+metricInputs.size() + " metrics at tick: "+ tickScope.toDisplayString());

        this.aggregateMetricGrid = createAggregateGrid(tickScope.getFrom());

        fillAggregateGrid(aggregateMetricGrid, metricInputs);
        aggregateMetricGrid.finish();

        output.breatheOut(torchieId, tickScope, aggregateMetricGrid);

        return aggregateMetricGrid;
    }

    @Override
    public MusicGridResults playAllTracks() {
        return aggregateMetricGrid.playAllTracks();
    }

    protected abstract void fillAggregateGrid(AggregateMetricGrid aggregateMetricGrid, List<T> metricInputs);

    private AggregateMetricGrid createAggregateGrid(GeometryClock.GridTime gridTime) {

        MusicClock musicClock = new MusicClock(gridTime);

        return new AggregateMetricGrid(gridTime, musicClock);
    }

}


