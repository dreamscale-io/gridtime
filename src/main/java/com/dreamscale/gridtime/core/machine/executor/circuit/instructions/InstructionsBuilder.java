package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.machine.executor.program.ProgramFactory;
import com.dreamscale.gridtime.core.machine.executor.program.SourceTileGeneratorProgram;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class InstructionsBuilder {
    private final UUID torchieId;
    private final TorchieState torchieState;
    private final ProgramFactory programFactory;

    public InstructionsBuilder(UUID torchieId, TorchieState torchieState, ProgramFactory programFactory) {
        this.torchieId = torchieId;
        this.torchieState = torchieState;
        this.programFactory = programFactory;
    }

    public TickInstructions gotoTile(ZoomLevel zoomLevel, LocalDateTime clockPosition) {
        GeometryClock.GridTime gotoGridTime = GeometryClock.createGridTime(zoomLevel, clockPosition);

        return new GotoTile(torchieState, gotoGridTime);
    }

    public TickInstructions regenerateTile(ZoomLevel zoom, LocalDateTime tileTime) {
        TorchieState forkedState = torchieState.fork();

        GeometryClock.GridTime gridTimeFrom = GeometryClock.createGridTime(zoom, tileTime);
        GeometryClock.GridTime gridTimeTo = gridTimeFrom.panRight();

        SourceTileGeneratorProgram regenProgram = programFactory.createBaseTileRegenProgram(torchieId, forkedState,
                gridTimeFrom.getClockTime(), gridTimeTo.getClockTime());

        Metronome.TickScope tickScope = new Metronome.TickScope(gridTimeFrom, gridTimeTo);

        TickInstructions instructions = null;
        if (zoom.equals(ZoomLevel.TWENTY)) {
            instructions = regenProgram.generateBaseTickInstructions(tickScope);
        } else {
            instructions = regenProgram.generateAggregateTickInstructions(tickScope);
        }
        return instructions;
    }

    public TickInstructions playTrack(TrackSetKey trackSetName) {
        return new PlayTrack(torchieState, trackSetName);
    }

    public TickInstructions playTile() {
        return new PlayTile(torchieState);
    }

    public TickInstructions nextTile() {
        return new ManuallyForwardNextTile(torchieState);
    }



}
