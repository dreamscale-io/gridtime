package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ManuallyForwardNextTile extends TileInstructions {

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
