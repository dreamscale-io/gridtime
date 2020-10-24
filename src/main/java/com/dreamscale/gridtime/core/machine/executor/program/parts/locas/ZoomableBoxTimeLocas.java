package com.dreamscale.gridtime.core.machine.executor.program.parts.locas;

import com.dreamscale.gridtime.api.grid.Results;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.id.TorchieHashId;
import com.dreamscale.gridtime.core.machine.memory.grid.CompositeBoxGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class ZoomableBoxTimeLocas<T> implements Locas {

    private final UUID torchieId;
    private final InputStrategy<T> input;
    private final OutputStrategy output;

    private final FeatureCache featureCache;

    private CompositeBoxGrid compositeBoxGrid;

    public ZoomableBoxTimeLocas(UUID torchieId, FeatureCache featureCache,
                                InputStrategy<T> input,
                                OutputStrategy output) {
        this.torchieId = torchieId;
        this.featureCache = featureCache;
        this.input = input;
        this.output = output;

    }

    @Override
    public CompositeBoxGrid runProgram(Metronome.TickScope tickScope) {

        TorchieHashId torchieHash = new TorchieHashId(torchieId);

        String zoomGridId = "BoxGrid:Id:@tile"+torchieHash.toDisplayString() + tickScope.getFrom().toDisplayString();

        List<T> metricInputs = input.breatheIn(torchieId, tickScope);

        log.debug(zoomGridId + ": Found "+metricInputs.size() + " input metrics");

        this.compositeBoxGrid = createCompositeBoxGrid(zoomGridId, tickScope.getFrom());

        fillCompositeZoomGrid(compositeBoxGrid, featureCache, metricInputs);
        compositeBoxGrid.finish();

        int recordsSaved = output.breatheOut(torchieId, tickScope, compositeBoxGrid);

        log.debug(zoomGridId + ": Saved "+recordsSaved + " output metrics");

        return compositeBoxGrid;
    }

    @Override
    public Results playAllTracks() {
        return compositeBoxGrid.playAllTracks();
    }

    protected abstract void fillCompositeZoomGrid(CompositeBoxGrid boxZoomGrid,
                                                  FeatureCache featureCache, List<T> metricInputs);

    private CompositeBoxGrid createCompositeBoxGrid(String gridId, GeometryClock.GridTime gridTime) {

        MusicClock musicClock = new MusicClock(gridTime);

        return new CompositeBoxGrid(gridId, gridTime, musicClock);
    }

}


