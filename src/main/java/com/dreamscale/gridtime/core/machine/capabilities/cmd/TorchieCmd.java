package com.dreamscale.gridtime.core.machine.capabilities.cmd;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.GridTimeExecutor;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyTrigger;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results;
import com.dreamscale.gridtime.core.machine.memory.type.CmdType;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
public class TorchieCmd {


    private final Torchie torchie;

    private boolean syncCommandInProgress;

    private final NotifyTrigger NOTIFY_WHEN_DONE;
    private final NotifyTrigger LOG_EXECUTION_DONE;

    private static final int MAX_WAIT_LOOPS = 10;

    private final GridTimeExecutor torchieExecutor;

    public TorchieCmd(GridTimeExecutor executorPool, Torchie torchie) {
        this.torchieExecutor = executorPool;
        this.torchie = torchie;
        this.syncCommandInProgress = false;

        //trigger "constants"

        NOTIFY_WHEN_DONE = new UpdateCommandInProgressTrigger();
        LOG_EXECUTION_DONE = new LogExecutionDoneTrigger();
    }

    public void runProgram() {
        syncCommandInProgress = true;

        torchie.notifyWhenProgramDone(NOTIFY_WHEN_DONE);

        torchieExecutor.startTorchieIfNotActive(torchie);

        waitForCommandToFinish();
    }

    public void gotoTile(ZoomLevel zoom, LocalDateTime tileTime) {
        TileInstructions instructions = torchie.getInstructionsBuilder().gotoTile(zoom, tileTime);
        runInstructionAndWaitTilDone(instructions);
    }

    public void nextTile() {
        TileInstructions instructions = torchie.getInstructionsBuilder().nextTile();
        runInstructionAndWaitTilDone(instructions);
    }

    public MusicGridResults playTile() {
        TileInstructions instructions = torchie.getInstructionsBuilder().playTile();
        runInstructionAndWaitTilDone(instructions);

        return (MusicGridResults) instructions.getOutputResult();
    }

    public MusicGridResults playTrack(TrackSetKey trackSetName) {
        TileInstructions instructions = torchie.getInstructionsBuilder().playTrack(trackSetName);
        runInstructionAndWaitTilDone(instructions);

        return (MusicGridResults) instructions.getOutputResult();
    }

    public void haltProgram() {
        torchie.haltProgram();
    }

    public void resumeProgram() {
        torchie.resumeProgram();
    }

    private void runInstructionAndWaitTilDone(TileInstructions instructions) {
        if (syncCommandInProgress == true) {
            log.error("Already in progress!");
        }

        syncCommandInProgress = true;

        instructions.addTriggerToNotifyList(NOTIFY_WHEN_DONE);

        torchie.scheduleInstruction(instructions);
        torchieExecutor.startTorchieIfNotActive(torchie);

        waitForCommandToFinish();

    }

    public List<Results> runSyncCommand(CmdType cmdType, String cmdStr) throws InterruptedException {

        Map<String, String> params = cmdType.extractParameters(cmdStr);

        return runSyncCommand(cmdType, params);
    }

    public List<Results> runSyncCommand(CmdType cmdType, Map<String, String> templateParameters) throws InterruptedException {

        syncCommandInProgress = true;

        TileInstructions tileInstructions = generateTileInstructions(cmdType, templateParameters);
        tileInstructions.addTriggerToNotifyList(LOG_EXECUTION_DONE);


        scheduleInstruction(tileInstructions);

        waitForCommandToFinish();

        return tileInstructions.getAllOutputResults();
    }

    public void runCommand(NotifyTrigger notify, CmdType cmdType, Map<String, String> templateParameters) {

        TileInstructions tileInstructions = generateTileInstructions(cmdType, templateParameters);
        tileInstructions.addTriggerToNotifyList(LOG_EXECUTION_DONE);
        tileInstructions.addTriggerToNotifyList(notify);

        scheduleInstruction(tileInstructions);
    }

    private void scheduleInstruction(TileInstructions instructions) {
        torchie.scheduleInstruction(instructions);

        torchieExecutor.startTorchieIfNotActive(torchie);
    }

    private void waitForCommandToFinishWithTimeout(int millis) {

    }

    private void waitForCommandToFinish() {
        int waitLoopCounter = MAX_WAIT_LOOPS;
        try {
            while (syncCommandInProgress && waitLoopCounter > 0) {
                log.info("Waiting for cmd finish!");
                Thread.sleep(100);
                waitLoopCounter--;
            }
            if (waitLoopCounter == 0) {
                log.error("Wait loop count exceeded");
            }
        } catch (InterruptedException ex) {
            log.error("Interrupted", ex);
        }

    }

    private TileInstructions generateTileInstructions(CmdType cmdType, Map<String, String> templateParameters) {
        //TODO need to be able to parse commands, and then be able to run sync commands from the UI

        return null;
    }




    private class UpdateCommandInProgressTrigger implements NotifyTrigger {
        @Override
        public void notifyWhenDone(TileInstructions instructions, List<Results> results) {
            log.debug("Setting cmd in progress to false");
            syncCommandInProgress = false;
        }
    }

    private class LogExecutionDoneTrigger implements NotifyTrigger {

        @Override
        public void notifyWhenDone(TileInstructions instructions, List<Results> results) {
            log.info("Torchie "+torchie.getTorchieId() + " completed command `" + instructions.getCmdDescription() +
                    "` in "+instructions.getExecutionDuration()
                    + " with queue time: "+ instructions.getQueueDuration());
        }
    }
}
