package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GotoTile extends TickInstructions {

    private final TorchieState torchieState;

    private final GeometryClock.GridTime gotoPosition;


    public GotoTile(TorchieState torchieState, GeometryClock.GridTime gotoPosition ) {
        this.torchieState = torchieState;
        this.gotoPosition = gotoPosition;
    }

    @Override
    public void executeInstruction() throws InterruptedException {
        torchieState.gotoPosition(gotoPosition);

        setOutputTile(torchieState.getActiveTile());
    }

    @Override
    public String getCmdDescription() {
        return "goto tile at "+gotoPosition.toDisplayString();
    }
}
