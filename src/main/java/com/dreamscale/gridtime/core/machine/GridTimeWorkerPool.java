package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.gridtime.core.machine.executor.worker.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GridTimeWorkerPool implements WorkerPool {

    @Autowired
    private TorchieWorkerPool torchieWorkerPool;

    @Autowired
    private AggregationWorkerPool aggregateWorkerPool;

    private boolean lastInstructionIsTorchie = false;

    public boolean hasWork() {

        torchieWorkerPool.sync();

        return torchieWorkerPool.hasWork() || aggregateWorkerPool.hasWork();
    }

    public TorchieCmd getTorchieCmd(UUID torchieId) {

        return torchieWorkerPool.getTorchieCmd(torchieId);
    }


    public TileInstructions whatsNext() {

        TileInstructions instructions = null;

        if (aggregateWorkerPool.hasWork()) {
            instructions = aggregateWorkerPool.whatsNext();
            if (instructions != null) {
                lastInstructionIsTorchie = false;
            }
        }

        if (instructions == null) {
            instructions = torchieWorkerPool.whatsNext();
            lastInstructionIsTorchie = true;
        }

        return instructions;
    }

    public void evictLastWorker() {
        if (lastInstructionIsTorchie) {
            torchieWorkerPool.evictLastWorker();
        }
    }

    @Override
    public int size() {
        return torchieWorkerPool.size() + aggregateWorkerPool.size();
    }


    public void addWorker(Torchie torchie) {
        torchieWorkerPool.addWorker(torchie.getTorchieId(), torchie);
    }

    public void clear() {
        torchieWorkerPool.clear();
    }
}




