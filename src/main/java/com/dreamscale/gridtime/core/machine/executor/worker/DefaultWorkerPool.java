package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;

import java.util.UUID;

public class DefaultWorkerPool implements WorkerPool, LiveQueue {

    WhatsNextWheel<TileInstructions> whatsNextWheel = new WhatsNextWheel<>();

    private TileInstructions peekInstruction;

    public void addTorchie(Torchie torchie) {
        whatsNextWheel.addWorker(torchie.getTorchieId(), torchie);
    }

    public void addWorker(UUID workerId, Worker<TileInstructions> worker) {
        whatsNextWheel.addWorker(workerId, worker);
    }

    @Override
    public boolean hasWork() {
        if (peekInstruction == null) {
            peek();
        }
        return peekInstruction != null;
    }

    @Override
    public void evictLastWorker() {
        whatsNextWheel.evictLastWorker();
    }

    @Override
    public int size() {
        return whatsNextWheel.size();
    }


    @Override
    public TileInstructions whatsNext() {

        if (peekInstruction == null) {
            peek();
        }

        TileInstructions nextInstruction = peekInstruction;

        peekInstruction = null;

        return nextInstruction;
    }

    private void peek() {

        if (peekInstruction == null) {
            peekInstruction = whatsNextWheel.whatsNext();

            while (peekInstruction == null && whatsNextWheel.isNotExhausted()) {

                whatsNextWheel.evictLastWorker();
                peekInstruction = whatsNextWheel.whatsNext();

            }
        }
    }

    @Override
    public void submit(UUID workerId, Worker<TileInstructions> worker) {
        whatsNextWheel.submit(workerId, worker);
    }
}
