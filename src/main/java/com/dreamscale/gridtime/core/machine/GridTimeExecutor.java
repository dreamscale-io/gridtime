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

    private int ticks = 0;

    private AtomicBoolean isGameLoopRunning;

    private static final int LOOK_FOR_MORE_WORK_DELAY = 1;
    private static final int MAX_WORK_CAPACITY = 10;

    private long executorStartTime = 0;
    private int stopAfterTicks = 0;
    private long stopAfterTime = 0;
    private Future<?> gameLoopFuture = null;
    private boolean stopAfterIdle;

    public GridTimeExecutor(WorkPile workPile) {
        this.workPile = workPile;
        this.isGameLoopRunning = new AtomicBoolean(false);
    }

    public void shutdown() {
        isGameLoopRunning.set(false);

        if (executorPool != null) {
            executorPool.shutdown();
            executorPool = null;
        }
    }

    public void start() {
        this.executorPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_WORK_CAPACITY + 1);

        if (!isGameLoopRunning.getAndSet(true)) {
            this.executorStartTime = System.currentTimeMillis();
            gameLoopFuture = executorPool.submit(new GameLoopRunner());
        }
    }

    public void reset() {
        executorStartTime = System.currentTimeMillis();
        ticks = 0;
        stopAfterIdle = false;

        if (executorPool != null && isGameLoopRunning.get() == false) {

            if (!isGameLoopRunning.getAndSet(true)) {
                gameLoopFuture = executorPool.submit(new GameLoopRunner());
            }
        }
    }

    public void waitForDone() {

        try {
            if (gameLoopFuture != null) {
                gameLoopFuture.get();
            }

        } catch (Exception ex) {
            log.error("Interrupted", ex);
        }

    }

    public void waitForDone(int timeoutMillis) {

        try {
            if (gameLoopFuture != null) {
                gameLoopFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);
            }

        } catch (Exception ex) {
            log.error("Interrupted", ex);
        }

    }


    private boolean hasPoolCapacityForMoreWork() {

//        log.info("executor tasks  = "+executorPool.getTaskCount());
//        log.info("executor active = "+executorPool.getActiveCount());
//        log.info("executor queue = "+executorPool.getQueue().size());

        return executorPool.getActiveCount() <= MAX_WORK_CAPACITY;
    }

    public void configureDoneAfterTicks(int ticksToWait) {
        this.stopAfterTicks = ticksToWait;
    }

    public void configureDoneAfterTime(long millisToWait) {
        this.stopAfterTime = millisToWait;
    }

    private void stopIfConditionMet(long currentTimeMillis) {
        if (stopAfterTicks > 0 && ticks >= stopAfterTicks) {
            isGameLoopRunning.set(false);
        }

        if (stopAfterTime > 0 && (currentTimeMillis - executorStartTime) >= stopAfterTime) {
            isGameLoopRunning.set(false);
        }
    }

    public int getTicks() {
        return ticks;
    }

    public void configureDoneAfterIdle() {
        this.stopAfterIdle = true;
    }


    private class GameLoopRunner implements Runnable {


        long currentTimeMillis = 0;

        @Override
        public void run() {
            try {
                log.info("Starting up GridTime GameLoop");

                while (isGameLoopRunning.get()) {
                    //fairly round robin with all active torchies,
                    //whenever there is room in the executor pool

                    while (hasPoolCapacityForMoreWork() && workPile.hasWork()) {
                        TickInstructions instruction = workPile.whatsNext();
                        if (instruction != null) {
                            log.info("Submitting instruction: " + instruction.getCmdDescription());

                            ticks++;
                            log.info("ticks = " + ticks);
                            currentTimeMillis = System.currentTimeMillis();

                            executorPool.submit(instruction);
                        } else {
                            log.warn("Null instruction");
                        }

                        stopIfConditionMet(currentTimeMillis);

                        if (!isGameLoopRunning.get()) {
                            log.debug("Exiting game loop");
                            break;
                        }
                    }
                    stopIfConditionMet(currentTimeMillis);
                    Thread.sleep(LOOK_FOR_MORE_WORK_DELAY);
                }
                log.debug("About to exit");
            } catch (Exception ex) {
                log.error("Executor GameLoop halted", ex);
                isGameLoopRunning.set(false);
            }
        }

    }


}
