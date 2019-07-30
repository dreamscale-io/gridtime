package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.input.InputStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.output.OutputStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.AggregateGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class Locas<T> {

    private final UUID torchieId;
    private final FeatureCache featureCache;
    private final InputStrategy<T> input;
    private final OutputStrategy output;
    private AggregateGrid aggregateGrid;

    public Locas(UUID torchieId, FeatureCache featureCache,
                 InputStrategy<T> input,
                 OutputStrategy output) {
        this.torchieId = torchieId;
        this.featureCache = featureCache;
        this.input = input;
        this.output = output;

    }

    public void runProgram(Metronome.Tick tick) {
        List<T> metricInputs = input.breatheIn(torchieId, tick);

        log.debug("Found "+metricInputs.size() + " metrics at tick: "+tick.toDisplayString());

        this.aggregateGrid = createAggregateGrid(tick.getFrom());

        fillAggregateGrid(aggregateGrid, metricInputs);

        output.breatheOut(torchieId, tick, aggregateGrid);
    }

    public MusicGridResults playAllTracks() {
        return aggregateGrid.playAllTracks();
    }

    protected abstract void fillAggregateGrid(AggregateGrid aggregateGrid, List<T> metricInputs);

    private AggregateGrid createAggregateGrid(GeometryClock.GridTime gridTime) {

        MusicClock musicClock = new MusicClock(gridTime);

        return new AggregateGrid(featureCache, gridTime, musicClock);
    }

}


