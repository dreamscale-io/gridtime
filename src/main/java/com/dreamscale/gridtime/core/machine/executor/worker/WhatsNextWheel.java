package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.NoOpInstruction;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class WhatsNextWheel {

    private Map<UUID, Worker> workerMap = DefaultCollections.map();

    private final LinkedList<UUID> nextWorkerQueue = new LinkedList<>();

    private Map<UUID, LocalDateTime> markedForEviction = new HashMap<>();

    private final int SECONDS_TIL_EVICTION = 30;

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
            throw new RuntimeException("Adding duplicate worker already in map! "+workerId);
        }
    }

    public void evictLastWorker() {
        evictWorker(getLastWorker());
    }

    public Set<UUID> getWorkerKeys() {
        return new HashSet<>(workerMap.keySet());
    }

    public void evictWorker(UUID workerId) {
        log.debug("evicting worker " + workerId);
        nextWorkerQueue.remove(workerId);
        markedForEviction.remove(workerId);
        workerMap.remove(workerId);

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

    public void clear() {
        workerMap.clear();
        nextWorkerQueue.clear();
    }

    public void unmarkForEviction(UUID workerId) {
        synchronized (markedForEviction) {
            if (markedForEviction.containsKey(workerId)) {
                markedForEviction.remove(workerId);
                nextWorkerQueue.add(workerId);
            }

        }
    }

    public void markLastForEviction(LocalDateTime now) {
        UUID lastWorkerId = getLastWorker();

        synchronized (markedForEviction) {
            markedForEviction.putIfAbsent(lastWorkerId, now);
            nextWorkerQueue.remove(lastWorkerId);
        }
    }


    public void markForEviction(UUID torchieId, LocalDateTime now) {
        synchronized (markedForEviction) {
            markedForEviction.putIfAbsent(torchieId, now);
            nextWorkerQueue.remove(torchieId);
        }
    }

    public List<UUID> purgeEvicted(LocalDateTime now) {
        List<UUID> purged = new ArrayList<>();

        synchronized (markedForEviction) {
            Iterator<Map.Entry<UUID, LocalDateTime>> evictIter = markedForEviction.entrySet().iterator();

            while (evictIter.hasNext()) {
                Map.Entry<UUID, LocalDateTime> entry = evictIter.next();
                if (entry.getValue().isBefore(now.minusSeconds(SECONDS_TIL_EVICTION))) {
                    workerMap.remove(entry.getKey());
                    purged.add(entry.getKey());
                    evictIter.remove();
                }

            }
        }
        return purged;
    }

    void abortAllPrograms() {
        for (Worker worker : workerMap.values()) {
            worker.abortAndClearProgram();
        }
    }

}
