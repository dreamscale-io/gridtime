package com.dreamscale.gridtime.core.machine.executor.program.parts.locas;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.TeamGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class ZoomableTeamLocas<T> implements Locas {

    private final UUID teamId;
    private final InputStrategy<T> input;
    private final OutputStrategy output;
    private TeamGrid teamGrid;

    public ZoomableTeamLocas(UUID teamId,
                             InputStrategy<T> input,
                             OutputStrategy output) {
        this.teamId = teamId;
        this.input = input;
        this.output = output;
    }

    @Override
    public IMusicGrid runProgram(Metronome.Tick tick) {
        List<T> metricInputs = input.breatheIn(teamId, tick);

        log.debug("Found "+metricInputs.size() + " metrics at tick: "+tick.toDisplayString());

        this.teamGrid = createTeamGrid(tick.getFrom());

        fillTeamGrid(teamGrid, metricInputs);
        teamGrid.finish();

        output.breatheOut(teamId, tick, teamGrid);

        return teamGrid;
    }

    @Override
    public MusicGridResults playAllTracks() {
        return teamGrid.playAllTracks();
    }

    protected abstract void fillTeamGrid(TeamGrid teamGrid, List<T> metricInputs);

    private TeamGrid createTeamGrid(GeometryClock.GridTime gridTime) {

        return new TeamGrid(gridTime);
    }

}

