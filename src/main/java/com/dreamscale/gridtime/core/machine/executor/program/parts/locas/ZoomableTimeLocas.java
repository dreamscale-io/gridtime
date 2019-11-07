package com.dreamscale.gridtime.core.machine.executor.program.parts.locas;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.id.TeamHashId;
import com.dreamscale.gridtime.core.machine.memory.feature.id.TorchieHashId;
import com.dreamscale.gridtime.core.machine.memory.grid.ZoomGrid;
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

    private ZoomGrid zoomGrid;

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
    public ZoomGrid runProgram(Metronome.TickScope tickScope) {

        TorchieHashId torchieHash = new TorchieHashId(torchieId);

        String zoomGridId = "ZoomGrid:Id:@tile"+ torchieHash.toDisplayString() +
                tickScope.getFrom().toDisplayString();

        List<T> metricInputs = input.breatheIn(teamId, torchieId, tickScope);

        log.debug(zoomGridId + ": Found "+metricInputs.size() + " input metrics");

        this.zoomGrid = createZoomGrid(zoomGridId, tickScope.getFrom());

        fillZoomGrid(zoomGrid, metricInputs);
        zoomGrid.finish();

        int recordsSaved = output.breatheOut(torchieId, tickScope, zoomGrid);

        log.debug(zoomGridId + ": Saved "+recordsSaved + " output metrics");

        return zoomGrid;
    }

    @Override
    public Results playAllTracks() {
        return zoomGrid.playAllTracks();
    }

    protected abstract void fillZoomGrid(ZoomGrid zoomGrid, List<T> metricInputs);

    private ZoomGrid createZoomGrid(String gridTitle, GeometryClock.GridTime gridTime) {

        MusicClock musicClock = new MusicClock(gridTime);

        return new ZoomGrid(gridTitle, gridTime, musicClock);
    }

}


