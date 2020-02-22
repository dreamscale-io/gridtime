package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ManuallyForwardNextTile extends TickInstructions {

    private final TorchieState torchieState;

    public ManuallyForwardNextTile(TorchieState torchieState) {
        this.torchieState = torchieState;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {

        GridTile activeTile = torchieState.getActiveTile();
        if (activeTile != null) {
            GeometryClock.GridTime nextGridTime = activeTile.getGridTime().panRight();
            log.debug("Forwarding tile to "+ nextGridTime.toDisplayString());

            torchieState.gotoPosition(nextGridTime);
        } else {
            log.error("Unable to forward to next tile, active tile is null");
        }

    }

    @Override
    public String getCmdDescription() {
        return "manually ticking forward next tile";
    }
}
