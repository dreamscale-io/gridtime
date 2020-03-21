package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WhatsNextWheel<T> implements LiveQueue {

    private Map<UUID, Worker<T>> workerMap = DefaultCollections.map();

    private final LinkedList<UUID> nextWorkerQueue = new LinkedList<>();

    public T whatsNext() {
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

    public UUID getLastWorker() {
        return nextWorkerQueue.getLast();
    }

    private boolean isWorkerReady(UUID workerId) {
        return workerMap.get(workerId).isWorkerReady();
    }

    public void addWorker(UUID workerId, Worker<T> worker) {
        nextWorkerQueue.add(workerId);
        workerMap.put(workerId, worker);
    }

    public void evictLastWorker() {
        UUID lastWorkerId = getLastWorker();
        evictWorker(lastWorkerId);
    }

    public Set<UUID> getWorkerKeys() {
        return workerMap.keySet();
    }

    public void evictWorker(UUID workerId) {
        nextWorkerQueue.remove(workerId);
        Worker<T> worker = workerMap.remove(workerId);

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
