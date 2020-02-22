package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.worker.WorkPile;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class GridTimeExecutor {

    private WorkPile workPile;
    private ThreadPoolExecutor executorPool;

    private AtomicBoolean isGameLoopRunning;

    private static final int LOOK_FOR_MORE_WORK_DELAY = 100;
    private static final int MAX_WORK_CAPACITY = 10;

    public GridTimeExecutor(WorkPile workPile) {
        this.workPile = workPile;
        this.executorPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_WORK_CAPACITY + 1);

        this.isGameLoopRunning = new AtomicBoolean(false);
    }

    public void shutdown() {
        isGameLoopRunning.set(false);
        executorPool.shutdown();
    }

    public void start() {

        if (!isGameLoopRunning.getAndSet(true)) {
            executorPool.submit(new GameLoopRunner());
        }
    }


    private boolean hasPoolCapacityForMoreWork() {

//        log.info("executor tasks  = "+executorPool.getTaskCount());
//        log.info("executor active = "+executorPool.getActiveCount());
//        log.info("executor queue = "+executorPool.getQueue().size());

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
                    //log.info("tick");
                    while (hasPoolCapacityForMoreWork() && workPile.hasWork()) {
                        TickInstructions instruction = workPile.whatsNext();

                        if (instruction != null) {
                            log.info("Submitting instruction: "+instruction.getCmdDescription());

                            executorPool.submit(instruction);
                        } else {
                            log.warn("Null instruction");
                            workPile.evictLastWorker();
                        }
                    }
                    //log.info("sleeping");
                    Thread.sleep(LOOK_FOR_MORE_WORK_DELAY);
                }
            } catch (InterruptedException ex) {
                log.error("Executor GameLoop halted by interrupt", ex);
            }
        }

    }



}
