package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.gridtime.core.machine.executor.worker.WorkerPool;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class GridTimeExecutor {

    private WorkerPool workerPool;

    private ThreadPoolExecutor executorPool;

    private AtomicBoolean isGameLoopRunning;

    private static final int LOOK_FOR_MORE_WORK_DELAY = 100;

    private static final int MAX_WORK_CAPACITY = 10;


    public GridTimeExecutor(WorkerPool workerPool) {
        this.workerPool = workerPool;
        this.executorPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_WORK_CAPACITY + 1);

        this.isGameLoopRunning = new AtomicBoolean(false);
    }

    public void shutdown() {
        isGameLoopRunning.set(false);
        executorPool.shutdown();
    }

    public void startTorchieIfNotActive(Torchie torchie) {
        workerPool.addWorker(torchie);

        if (!isGameLoopRunning.get()) {
            log.info("Starting game loop");
            startExecutorGameLoop();
        }
    }

    private void startExecutorGameLoop() {
        isGameLoopRunning.set(true);
        executorPool.submit(new GameLoopRunner());
    }

    public boolean contains(UUID torchieId) {
        return workerPool.containsWorker(torchieId);
    }

    public Torchie getTorchie(UUID torchieId) {
        return (Torchie) workerPool.getWorker(torchieId);
    }


    private boolean hasPoolCapacityForMoreWork() {

        log.info("executor tasks  = "+executorPool.getTaskCount());
        log.info("executor active = "+executorPool.getActiveCount());
        log.info("executor queue = "+executorPool.getQueue().size());

        return executorPool.getActiveCount() <= MAX_WORK_CAPACITY;
    }

    private class GameLoopRunner implements Runnable {

        @Override
        public void run() {
            try {
                log.info("Starting up GridTime GameLoop");
                while (isGameLoopRunning.get()) {
                    //fairly round robin with all active torchies,
                    //whenever there is room in the executor pool
                    log.info("tick");
                    while (hasPoolCapacityForMoreWork() && workerPool.hasWork()) {
                        TileInstructions instruction = workerPool.whatsNext();

                        if (instruction != null) {
                            log.info("Submitting instruction: "+instruction.getCmdDescription());

                            executorPool.submit(instruction);
                        } else {
                            log.warn("Null instruction");
                            workerPool.evictLastWorker();
                        }
                    }
                    log.info("sleeping");
                    Thread.sleep(LOOK_FOR_MORE_WORK_DELAY);
                }
            } catch (InterruptedException ex) {
                log.error("Executor GameLoop halted by interrupt", ex);
            }
        }

    }



}
