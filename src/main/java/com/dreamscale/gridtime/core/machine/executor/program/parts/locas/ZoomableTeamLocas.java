package com.dreamscale.gridtime.core.machine.executor.program.parts.locas;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.TeamMetricGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class ZoomableTeamLocas<T> implements Locas {

    private final UUID teamId;
    private final InputStrategy<T> input;
    private final OutputStrategy output;
    private TeamMetricGrid teamMetricGrid;

    public ZoomableTeamLocas(UUID teamId,
                             InputStrategy<T> input,
                             OutputStrategy output) {
        this.teamId = teamId;
        this.input = input;
        this.output = output;
    }

    @Override
    public IMusicGrid runProgram(Metronome.TickScope tickScope) {
        List<T> metricInputs = input.breatheIn(teamId, teamId, tickScope);

        log.debug("Found "+metricInputs.size() + " metrics at tick: "+ tickScope.toDisplayString());

        this.teamMetricGrid = createTeamGrid(tickScope.getFrom());

        fillTeamGrid(teamMetricGrid, metricInputs);
        teamMetricGrid.finish();

        output.breatheOut(teamId, tickScope, teamMetricGrid);

        return teamMetricGrid;
    }

    @Override
    public MusicGridResults playAllTracks() {
        return teamMetricGrid.playAllTracks();
    }

    protected abstract void fillTeamGrid(TeamMetricGrid teamMetricGrid, List<T> metricInputs);

    private TeamMetricGrid createTeamGrid(GeometryClock.GridTime gridTime) {

        return new TeamMetricGrid(gridTime);
    }

}


