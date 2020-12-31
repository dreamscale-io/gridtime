package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.grid.GridStatus;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.worker.WorkPile;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
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

    private GridStatus status = GridStatus.STOPPED;

    private String lastError = null;

    public GridTimeExecutor(WorkPile workPile) {
        this.workPile = workPile;
        this.isGameLoopRunning = new AtomicBoolean(false);
        this.executorPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_WORK_CAPACITY + 1);
    }

    public SimpleStatusDto shutdown() {
        if (isGameLoopRunning.get()) {
            isGameLoopRunning.set(false);

            workPile.shutdown();

            waitForDone();

            return new SimpleStatusDto(Status.SUCCESS, "Gridtime shutdown in progress.");
        } else {
            return new SimpleStatusDto(Status.NO_ACTION, "Gridtime not running.");
        }
    }

    public SimpleStatusDto start() {

        if (!isGameLoopRunning.get()) {
            this.executorStartTime = System.currentTimeMillis();
            isGameLoopRunning.set(true);

            gameLoopFuture = executorPool.submit(new GameLoopRunner());

            workPile.start();

            status = GridStatus.RUNNING;

            return new SimpleStatusDto(Status.SUCCESS, "Gridtime engine starting.");
        } else {
            return new SimpleStatusDto(Status.NO_ACTION, "Gridtime already running.");
        }

    }

    public void reset() {
        executorStartTime = System.currentTimeMillis();
        ticks = 0;

        workPile.reset();

        shutdown();
    }

    public SimpleStatusDto restart() {
        reset();
        shutdown();
        start();

        if (isRunning()) {
            return new SimpleStatusDto(Status.SUCCESS, "Gridtime restarted.");
        } else {
            return new SimpleStatusDto(Status.FAILED, "Unable to restart. Gridtime not running.");
        }
    }

    public GridStatus getStatus() {
        return status;
    }

    public String getLastError() {
        return lastError;
    }

    public boolean isRunning() {
        return isGameLoopRunning.get() || (gameLoopFuture != null && !gameLoopFuture.isDone());
    }

    public void waitForDone() {

        try {
            if (gameLoopFuture != null) {
                log.debug("Waiting for game loop exit");
                gameLoopFuture.get();

                log.debug("Game loop complete");
                gameLoopFuture = null;
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
            workPile.shutdown();
        }

        if (stopAfterTime > 0 && (currentTimeMillis - executorStartTime) >= stopAfterTime) {
            isGameLoopRunning.set(false);
            workPile.shutdown();
        }
    }

    public int getTicks() {
        return ticks;
    }

    private class GameLoopRunner implements Runnable {

        long currentTimeMillis = 0;
        List<Future<TickInstructions>> futureList = new LinkedList<>();

        @Override
        public void run() {
            try {
                log.info("Starting up GridTime GameLoop");
                lastError = null;

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

                            Future<TickInstructions> future = executorPool.submit(instruction);
                            futureList.add(future);

                            purgeDoneFutures();

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

                finishAllFutures();

            } catch (Exception ex) {
                log.error("Executor GameLoop halted", ex);
                lastError = ex.getMessage();
            }

            isGameLoopRunning.set(false);
            status = GridStatus.STOPPED;
        }

        private void finishAllFutures() {
            try {
                for (Future<TickInstructions> future : futureList) {
                    future.get();
                }
            } catch (Exception ex) {
                log.error("Exception while waiting on future", ex);
            }

        }

        private void purgeDoneFutures() {
            futureList.removeIf(Future::isDone);
        }

    }


}
