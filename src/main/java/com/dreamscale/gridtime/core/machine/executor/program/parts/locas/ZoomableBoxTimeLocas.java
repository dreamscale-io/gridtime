package com.dreamscale.gridtime.core.machine.executor.program.parts.locas;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.BoxAggregateMetricGrid;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class ZoomableBoxTimeLocas<T> implements Locas {

    private final UUID teamId;
    private final UUID torchieId;
    private final InputStrategy<T> input;
    private final OutputStrategy output;

    private final FeatureCache featureCache;

    private BoxAggregateMetricGrid aggregateMetricGrid;

    public ZoomableBoxTimeLocas(UUID teamId, UUID torchieId, FeatureCache featureCache,
                                InputStrategy<T> input,
                                OutputStrategy output) {
        this.teamId = teamId;
        this.torchieId = torchieId;
        this.featureCache = featureCache;
        this.input = input;
        this.output = output;

    }

    @Override
    public BoxAggregateMetricGrid runProgram(Metronome.TickScope tickScope) {
        List<T> metricInputs = input.breatheIn(teamId, torchieId, tickScope);

        log.debug("Found "+metricInputs.size() + " metrics at tick: "+ tickScope.toDisplayString());

        this.aggregateMetricGrid = createAggregateGrid(tickScope.getFrom());

        fillAggregateGrid(aggregateMetricGrid, featureCache, metricInputs);
        aggregateMetricGrid.finish();

        output.breatheOut(torchieId, tickScope, aggregateMetricGrid);

        return aggregateMetricGrid;
    }

    @Override
    public MusicGridResults playAllTracks() {
        return aggregateMetricGrid.playAllTracks();
    }

    protected abstract void fillAggregateGrid(BoxAggregateMetricGrid aggregateMetricGrid,
                                              FeatureCache featureCache, List<T> metricInputs);

    private BoxAggregateMetricGrid createAggregateGrid(GeometryClock.GridTime gridTime) {

        MusicClock musicClock = new MusicClock(gridTime);

        return new BoxAggregateMetricGrid(gridTime, musicClock);
    }

}


