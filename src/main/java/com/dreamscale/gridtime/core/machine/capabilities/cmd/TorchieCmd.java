package com.dreamscale.gridtime.core.machine.capabilities.cmd;

import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifySeeTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.now.WatcherType;
import com.dreamscale.gridtime.core.machine.executor.worker.LiveQueue;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
public class TorchieCmd extends SyncCmd {

    private final Torchie torchie;
    private final LiveQueue liveTorchieQueue;

    public TorchieCmd(LiveQueue liveTorchieQueue, Torchie torchie) {
        this.liveTorchieQueue = liveTorchieQueue;
        this.torchie = torchie;
    }

    public UUID getTorchieId() {
        return torchie.getTorchieId();
    }

    public void runProgram() {
        log.debug("Running program to completion...");
        startCommandInProgress();

        configureNotify(torchie);

        liveTorchieQueue.submitToLiveQueue(torchie);

        waitForCommandToFinish();
    }


    public void runProgramUntil(LocalDateTime runUntil) {
        startCommandInProgress();

        torchie.haltProgram();
        torchie.waitForCircuitReady();

        GeometryClock.GridTime timeToWatch = GeometryClock.createGridTime(ZoomLevel.TWENTY, runUntil);
        torchie.watchForGridtime(timeToWatch, new HaltProgramTrigger());

        configureNotify(torchie);
        torchie.resumeProgram();

        liveTorchieQueue.submitToLiveQueue(torchie);
        waitForCommandToFinish();
    }



    private class HaltProgramTrigger implements NotifySeeTrigger {

        @Override
        public void notifyOnSee(WatcherType watcherType, Object objectSeen) {
            log.debug("Object seen, halting program! = "+objectSeen);
            torchie.haltProgram();
            getNotifyDoneTrigger().notifyWhenDone(torchie.getLastInstruction(), null);
        }
    }

    public void gotoTile(ZoomLevel zoom, LocalDateTime tileTime) {
        TickInstructions instructions = torchie.getInstructionsBuilder().gotoTile(zoom, tileTime);
        runInstructionAndWaitTilDone(instructions);
    }

    public void regenerateTile(ZoomLevel zoom, LocalDateTime tileTime) {
        TickInstructions instructions = torchie.getInstructionsBuilder().regenerateTile(zoom, tileTime);
        runInstructionAndWaitTilDone(instructions);
    }


    public void nextTile() {
        TickInstructions instructions = torchie.getInstructionsBuilder().nextTile();
        runInstructionAndWaitTilDone(instructions);
    }

    public GridTableResults playTile() {
        TickInstructions instructions = torchie.getInstructionsBuilder().playTile();
        runInstructionAndWaitTilDone(instructions);

        return (GridTableResults) instructions.getOutputResult();
    }

    public GridTableResults playTrack(TrackSetKey trackSetName) {
        TickInstructions instructions = torchie.getInstructionsBuilder().playTrack(trackSetName);
        runInstructionAndWaitTilDone(instructions);

        return (GridTableResults) instructions.getOutputResult();
    }

    public void haltProgram() {
        torchie.haltProgram();
    }

    public void resumeProgram() {
        torchie.resumeProgram();
    }

    private void runInstructionAndWaitTilDone(TickInstructions instructions) {
        startCommandInProgress();

        configureNotify(instructions);

        torchie.scheduleInstruction(instructions);
        liveTorchieQueue.submitToLiveQueue(torchie);

        waitForCommandToFinish();

    }


}
