package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;

import java.util.UUID;

public class DefaultWorkPile implements WorkPile, LiveQueue {

    WhatsNextWheel<TickInstructions> whatsNextWheel = new WhatsNextWheel<>();

    private TickInstructions peekInstruction;

    public void addTorchie(Torchie torchie) {
        whatsNextWheel.addWorker(torchie.getTorchieId(), torchie);
    }

    public void addWorker(UUID workerId, Worker<TickInstructions> worker) {
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
    public void reset() {
        //no op
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public TickInstructions whatsNext() {

        if (peekInstruction == null) {
            peek();
        }

        TickInstructions nextInstruction = peekInstruction;

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
    public void submit(UUID workerId, Worker<TickInstructions> worker) {
        whatsNextWheel.submit(workerId, worker);
    }
}
