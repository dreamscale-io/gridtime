package com.dreamscale.gridtime.core.machine.executor.program.parts.locas;

import com.dreamscale.gridtime.api.grid.Results;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.id.TeamHashId;
import com.dreamscale.gridtime.core.machine.memory.grid.CompositeTeamBoxGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class ZoomableBoxTeamLocas<T> implements Locas {

    private final UUID teamId;
    private final InputStrategy<T> input;
    private final OutputStrategy output;

    private final FeatureCache featureCache;

    private CompositeTeamBoxGrid compositeTeamBoxGrid;

    public ZoomableBoxTeamLocas(UUID teamId, FeatureCache featureCache,
                                InputStrategy<T> input,
                                OutputStrategy output) {
        this.teamId = teamId;
        this.featureCache = featureCache;
        this.input = input;
        this.output = output;

    }

    @Override
    public CompositeTeamBoxGrid runProgram(Metronome.TickScope tickScope) {

        TeamHashId teamHashId = new TeamHashId(teamId);

        String zoomGridId = "TeamBoxGrid:Id:@tile"+teamHashId.toDisplayString() + tickScope.getFrom().toDisplayString();

        List<T> metricInputs = input.breatheIn(teamId, teamId, tickScope);

        log.debug(zoomGridId + ": Found "+metricInputs.size() + " input metrics");

        this.compositeTeamBoxGrid = createCompositeTeamBoxGrid(zoomGridId, tickScope.getFrom());

        fillCompositeZoomGrid(compositeTeamBoxGrid, featureCache, metricInputs);
        compositeTeamBoxGrid.finish();

        int recordsSaved = output.breatheOut(teamId, tickScope, compositeTeamBoxGrid);

        log.debug(zoomGridId + ": Saved "+recordsSaved + " output metrics");

        return compositeTeamBoxGrid;
    }

    @Override
    public Results playAllTracks() {
        return compositeTeamBoxGrid.playAllTracks();
    }

    protected abstract void fillCompositeZoomGrid(CompositeTeamBoxGrid boxZoomGrid,
                                                  FeatureCache featureCache, List<T> metricInputs);

    private CompositeTeamBoxGrid createCompositeTeamBoxGrid(String gridId, GeometryClock.GridTime gridTime) {

        MusicClock musicClock = new MusicClock(gridTime);

        return new CompositeTeamBoxGrid(gridId, gridTime, musicClock);
    }

}


