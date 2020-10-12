package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.NoOpInstruction;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class WhatsNextWheel implements LiveQueue {

    private Map<UUID, Worker> workerMap = DefaultCollections.map();

    private final LinkedList<UUID> nextWorkerQueue = new LinkedList<>();

    public TickInstructions whatsNext() {
        int i = 0;

        while (i < nextWorkerQueue.size() ) {
            UUID workerId = nextWorkerQueue.get(i);
            if (isWorkerReady(workerId)) {
                nextWorkerQueue.remove(i);
                nextWorkerQueue.add(workerId);
                return workerMap.get(workerId).whatsNext();
            }
            i++;
        }

        return new NoOpInstruction();
    }

    public boolean isNotExhausted() {
        return nextWorkerQueue.size() > 0;
    }

    public UUID getLastWorker() {
        if (nextWorkerQueue.size() > 0) {
            return nextWorkerQueue.getLast();
        }
        return null;
    }

    private boolean isWorkerReady(UUID workerId) {
        boolean workerReady =  workerMap.get(workerId).isWorkerReady();

        return workerReady;
    }

    public void addWorker(UUID workerId, Worker worker) {
        log.debug("add worker "+workerId + " " + worker.getClass());

        if (!nextWorkerQueue.contains(workerId)) {
            nextWorkerQueue.add(workerId);
            workerMap.put(workerId, worker);
        } else {
            throw new RuntimeException("Adding torchie worker already in map! "+workerId);
        }
    }

    public void evictLastWorker() {
        UUID lastWorkerId = getLastWorker();
        log.debug("evict last worker "+lastWorkerId);
        evictWorker(lastWorkerId);
    }

    public Set<UUID> getWorkerKeys() {
        return new HashSet<>(workerMap.keySet());
    }

    public void evictWorker(UUID workerId) {
        log.debug("evicting worker " + workerId);
        nextWorkerQueue.remove(workerId);
        Worker worker = workerMap.remove(workerId);

    }

    public int size() {
        return nextWorkerQueue.size();
    }

    public boolean contains(UUID workerId) {
        return workerMap.containsKey(workerId);
    }

    public Worker getWorker(UUID workerId) {
        return workerMap.get(workerId);
    }

    @Override
    public void submit(UUID workerId, Worker worker) {

        if (!workerMap.containsKey(workerId)) {
            addWorker(workerId, worker);
        }
    }

    public void clear() {
        workerMap.clear();
        nextWorkerQueue.clear();
    }
}
