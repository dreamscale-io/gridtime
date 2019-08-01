package com.dreamscale.htmflow.core.gridtime.machine;

import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class GridTimeExecutor {

    private final int maxTorchieCapacity;

    private ThreadPoolExecutor executorPool;
    private final LinkedList<UUID> whatsNextQueue;

    private AtomicBoolean isGameLoopRunning;

    private static final int LOOK_FOR_MORE_WORK_DELAY = 100;

    private Map<UUID, Torchie> activeTorchiePool = DefaultCollections.map();

    public GridTimeExecutor(int maxTorchieCapacity) {
        this.maxTorchieCapacity = maxTorchieCapacity;
        this.executorPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxTorchieCapacity + 1);
        this.whatsNextQueue = new LinkedList<>();

        this.isGameLoopRunning = new AtomicBoolean(false);
    }

    public void startExecutorGameLoop() {
        isGameLoopRunning.set(true);
        executorPool.submit(new GameLoopRunner());
    }

    public void shutdown() {
        isGameLoopRunning.set(false);
        executorPool.shutdown();
    }

    public void startTorchieIfNotActive(Torchie torchie) {
        if (notInPool(torchie)) {
            addTorchieToJobPool(torchie);
        }
        if (!isGameLoopRunning.get()) {
            startExecutorGameLoop();
        }
    }

    public boolean contains(UUID torchieId) {
        return activeTorchiePool.containsKey(torchieId);
    }

    public Torchie getTorchie(UUID torchieId) {
        return activeTorchiePool.get(torchieId);
    }


    private boolean someTorchieIsReady() {

        int i = 0;
        boolean someTorchieIsReady = false;

        while (i < whatsNextQueue.size()) {
            UUID torchieId = whatsNextQueue.get(i);
            if (isTorchieReady(torchieId)) {
                log.info("Torchie is ready! "+torchieId);
                someTorchieIsReady = true;
                break;
            }
        }

        return someTorchieIsReady;
    }

    private UUID whatsNextTorchieInQueue() {
        int i = 0;

        UUID nextTorchieId = null;

        while (i < whatsNextQueue.size()) {
            UUID torchieId = whatsNextQueue.get(i);
            if (isTorchieReady(torchieId)) {
                whatsNextQueue.remove(i);
                whatsNextQueue.add(torchieId);
                nextTorchieId = torchieId;
                break;
            }
        }

       return nextTorchieId;
    }

    private boolean isTorchieReady(UUID torchieId) {
        Torchie torchie = activeTorchiePool.get(torchieId);
        return torchie.getCircuitMonitor().isReady();
    }

    private boolean hasPoolCapacityForMoreWork() {
        return executorPool.getQueue().size() == 0;
    }

    private void addTorchieToJobPool(Torchie torchie)  {

        activeTorchiePool.put(torchie.getTorchieId(), torchie);
        whatsNextQueue.add(torchie.getTorchieId());
    }

    private void removeTorchieFromPool(Torchie torchie) {

        torchie.serializeForSleep();

        whatsNextQueue.remove(torchie.getTorchieId());
        activeTorchiePool.remove(torchie.getTorchieId());

        //terminateLoopIfPoolEmpty();
    }

    private void terminateLoopIfPoolEmpty() {
        if (activeTorchiePool.size() == 0) {
            isGameLoopRunning.set(false);
        }
    }

    private boolean notInPool(Torchie torchie) {
        return !activeTorchiePool.containsKey(torchie.getTorchieId());
    }


    private List<Torchie> getActiveTorchies() {
        return new ArrayList<>(activeTorchiePool.values());
    }

    public List<CircuitMonitor> getAllTorchieMonitors() {
        List<CircuitMonitor> torchieMonitors = new ArrayList<>();

        for (Torchie torchie : getActiveTorchies()) {
            torchieMonitors.add(torchie.getCircuitMonitor());
        }
        return torchieMonitors;
    }

    private class GameLoopRunner implements Runnable {

        @Override
        public void run() {
            try {
                log.info("Starting up Torchie GameLoop");
                while (isGameLoopRunning.get()) {
                    //fairly round robin with all active torchies,
                    //whenever there is room in the executor pool
                    while (hasPoolCapacityForMoreWork() && someTorchieIsReady()) {

                        UUID whatsNextTorchieId = whatsNextTorchieInQueue();
                        Torchie torchie = getTorchie(whatsNextTorchieId);

                        TileInstructions instruction = torchie.whatsNext();

                        if (instruction != null) {
                            log.info("Submitting instruction: "+instruction.getCmdDescription());

                            executorPool.submit(instruction);
                        } else {
                            log.info("null instruction");
                            removeTorchieFromPool(torchie);
                        }
                    }
                    Thread.sleep(LOOK_FOR_MORE_WORK_DELAY);
                }
            } catch (InterruptedException ex) {
                log.error("Executor GameLoop halted by interrupt", ex);
            }
        }

    }



}
