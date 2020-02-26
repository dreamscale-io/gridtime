package com.dreamscale.gridtime.core.machine.executor.program.parts.locas;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.GridTableResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.id.TeamHashId;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.TeamZoomGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class ZoomableTeamLocas<T> implements Locas {

    private final UUID teamId;
    private final FeatureCache featureCache;

    private final InputStrategy<T> input;
    private final OutputStrategy output;

    private TeamZoomGrid teamZoomGrid;

    public ZoomableTeamLocas(UUID teamId, FeatureCache featureCache,
                             InputStrategy<T> input,
                             OutputStrategy output) {
        this.teamId = teamId;
        this.featureCache = featureCache;
        this.input = input;
        this.output = output;
    }

    @Override
    public IMusicGrid runProgram(Metronome.TickScope tickScope) {

        TeamHashId teamHash = new TeamHashId(teamId);

        String zoomGridId = "TeamZoomGrid:Id:@tile"+teamHash.toDisplayString() +
                tickScope.getFrom().toDisplayString();


        List<T> metricInputs = input.breatheIn(teamId, teamId, tickScope);

        log.debug(zoomGridId + ": Found "+metricInputs.size() + " input metrics");

        this.teamZoomGrid = createTeamZoomGrid(tickScope.getFrom());

        fillTeamGrid(teamZoomGrid, metricInputs);
        teamZoomGrid.finish();

        int recordsSaved = output.breatheOut(teamId, tickScope, teamZoomGrid);

        log.debug(zoomGridId + ": Saved "+recordsSaved + " output metrics");

        return teamZoomGrid;
    }

    @Override
    public GridTableResults playAllTracks() {
        return teamZoomGrid.playAllTracks();
    }

    protected abstract void fillTeamGrid(TeamZoomGrid teamZoomGrid, List<T> metricInputs);

    private TeamZoomGrid createTeamZoomGrid(GeometryClock.GridTime gridTime) {

        TeamHashId teamHashId = new TeamHashId(teamId);

        String title = "TeamZoomGrid:Id:@tile"+teamHashId.toDisplayString() + gridTime.toDisplayString();

        return new TeamZoomGrid(title, gridTime);
    }

}


