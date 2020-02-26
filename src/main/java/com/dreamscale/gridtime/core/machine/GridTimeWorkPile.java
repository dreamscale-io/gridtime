package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.monitor.CircuitActivityDashboard;
import com.dreamscale.gridtime.core.machine.executor.worker.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GridTimeWorkPile implements WorkPile {

    @Autowired
    CircuitActivityDashboard circuitActivityDashboard;

    //these are system base jobs, that are prioritized over all other work, like generating calendar
    @Autowired
    private SystemWorkPile systemWorkPile;

    //these are mid-length jobs, that might run for minutes, that generate source tiles requiring aggregation

    @Autowired
    private TorchieWorkPile torchieWorkPile;

    //these are a fixed pool of workers that handle all aggregation related work, triggered off of events

    @Autowired
    private PlexerWorkPile plexerWorkPile;

    private boolean lastInstructionIsTorchie = false;

    public boolean hasWork() {

        systemWorkPile.sync();

        torchieWorkPile.sync();

        return systemWorkPile.hasWork() || torchieWorkPile.hasWork() || plexerWorkPile.hasWork();
    }

    public TorchieCmd getTorchieCmd(UUID torchieId) {

        return torchieWorkPile.getTorchieCmd(torchieId);
    }

    public TickInstructions whatsNext() {

        TickInstructions instructions = null;

        if (circuitActivityDashboard.tickAndCheckIfNeedsRefresh()) {
            instructions = circuitActivityDashboard.generateRefreshTick();
            lastInstructionIsTorchie = false;
        }

        if (systemWorkPile.hasWork()) {
            instructions = systemWorkPile.whatsNext();
            if (instructions != null) {
                lastInstructionIsTorchie = false;
            }
        }

        if (plexerWorkPile.hasWork()) {
            instructions = plexerWorkPile.whatsNext();
            if (instructions != null) {
                lastInstructionIsTorchie = false;
            }
        }

        if (instructions == null) {
            instructions = torchieWorkPile.whatsNext();
            lastInstructionIsTorchie = true;
        }

        return instructions;
    }

    public void evictLastWorker() {
        if (lastInstructionIsTorchie) {
            torchieWorkPile.evictLastWorker();
        }
    }

    @Override
    public int size() {
        return systemWorkPile.size() + torchieWorkPile.size() + plexerWorkPile.size();
    }

    public TorchieCmd submitJob(Torchie torchie) {
        return torchieWorkPile.submitJob(torchie);
    }

    public void clear() {
        torchieWorkPile.clear();
    }

}




