package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;

import java.util.UUID;

public class TorchieWorkerPool implements WorkerPool {

    private final WhatsNextWheel whatsNextWheel;

    private TileInstructions peekInstruction;

    public TorchieWorkerPool() {
        this.whatsNextWheel = new WhatsNextWheel();
    }

    @Override
    public void addWorker(Worker worker) {
        if (worker instanceof Torchie) {
            Torchie torchie = (Torchie)worker;
            whatsNextWheel.addWorker(torchie.getTorchieId(), torchie);
        }
    }

    @Override
    public boolean containsWorker(UUID workerId) {
        return whatsNextWheel.contains(workerId);
    }

    public boolean containsWorker(Torchie torchie) {
        return whatsNextWheel.contains(torchie.getTorchieId());
    }

    @Override
    public Torchie getWorker(UUID workerId) {
        return (Torchie) whatsNextWheel.getWorker(workerId);
    }

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


}
