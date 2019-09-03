package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;

import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class WhatsNextWheel {

    private Map<UUID, Worker> workerMap = DefaultCollections.map();

    private final LinkedList<UUID> nextWorkerQueue = new LinkedList<>();

    public TileInstructions whatsNext() {
        int i = 0;

        while (i < nextWorkerQueue.size()) {
            UUID workerId = nextWorkerQueue.get(i);
            if (isWorkerReady(workerId)) {
                nextWorkerQueue.remove(i);
                nextWorkerQueue.add(workerId);
                return workerMap.get(workerId).whatsNext();
            }
        }

        return null;
    }

    public boolean isNotExhausted() {
        return nextWorkerQueue.size() > 0;
    }

    public void evictLastWorker() {
        UUID lastWorkerId = nextWorkerQueue.getLast();
        removeWorker(lastWorkerId);
    }

    private boolean isWorkerReady(UUID workerId) {
        return workerMap.get(workerId).isWorkerReady();
    }

    public void addWorker(UUID workerId, Worker worker) {
        nextWorkerQueue.add(workerId);
        workerMap.put(workerId, worker);
    }

    public void removeWorker(UUID workerId) {
        nextWorkerQueue.remove(workerId);
        workerMap.remove(workerId);
    }


    public boolean contains(UUID workerId) {
        return workerMap.containsKey(workerId);
    }

    public Worker getWorker(UUID workerId) {
        return workerMap.get(workerId);
    }
}
