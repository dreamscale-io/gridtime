package com.dreamscale.htmflow.core.gridtime.capabilities.cmd;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.kernel.Torchie;
import com.dreamscale.htmflow.core.gridtime.kernel.TorchiePoolExecutor;
import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit.NotifyTrigger;
import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Results;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.type.CmdType;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.key.TrackSetKey;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class TorchieCmd {


    private final Torchie torchie;

    private final AtomicBoolean syncCommandInProgress;

    private final NotifyTrigger NOTIFY_WHEN_DONE;
    private final NotifyTrigger LOG_EXECUTION_DONE;

    private final TorchiePoolExecutor torchieExecutor;

    public TorchieCmd(TorchiePoolExecutor executorPool, Torchie torchie) {
        this.torchieExecutor = executorPool;
        this.torchie = torchie;
        this.syncCommandInProgress = new AtomicBoolean(false);

        //trigger "constants"

        NOTIFY_WHEN_DONE = new UpdateCommandInProgressTrigger();
        LOG_EXECUTION_DONE = new LogExecutionDoneTrigger();
    }

    public void runProgram() {
        syncCommandInProgress.set(true);

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

        return (MusicGridResults) instructions.getOutputResults();
    }

    public MusicGridResults playTrack(TrackSetKey trackSetName) {
        TileInstructions instructions = torchie.getInstructionsBuilder().playTrack(trackSetName);
        runInstructionAndWaitTilDone(instructions);

        return (MusicGridResults) instructions.getOutputResults();
    }

    public void haltMetronome() {
        TileInstructions instructions = torchie.getInstructionsBuilder().haltMetronome();
        runInstructionAndWaitTilDone(instructions);
    }

    public void resumeMetronome() {
        TileInstructions instructions = torchie.getInstructionsBuilder().resumeMetronome();
        runInstructionAndWaitTilDone(instructions);
    }

    private void runInstructionAndWaitTilDone(TileInstructions instructions) {
        syncCommandInProgress.set(true);

        instructions.addTriggerToNotifyList(NOTIFY_WHEN_DONE);

        torchie.scheduleInstruction(instructions);
        torchieExecutor.startTorchieIfNotActive(torchie);

        waitForCommandToFinish();

    }

    public Results runSyncCommand(CmdType cmdType, String cmdStr) throws InterruptedException {

        Map<String, String> params = cmdType.extractParameters(cmdStr);

        return runSyncCommand(cmdType, params);
    }

    public Results runSyncCommand(CmdType cmdType, Map<String, String> templateParameters) throws InterruptedException {

        syncCommandInProgress.set(true);

        TileInstructions tileInstructions = generateTileInstructions(cmdType, templateParameters);
        tileInstructions.addTriggerToNotifyList(LOG_EXECUTION_DONE);


        scheduleInstruction(tileInstructions);

        waitForCommandToFinish();

        return tileInstructions.getOutputResults();
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

    private void waitForCommandToFinish() {
        try {
            while (syncCommandInProgress.get()) {
                Thread.sleep(100);
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
        public void notifyWhenDone(TileInstructions instructions, Results results) {
            log.debug("Setting cmd in progress to false");
            syncCommandInProgress.set(false);
        }
    }

    private class LogExecutionDoneTrigger implements NotifyTrigger {

        @Override
        public void notifyWhenDone(TileInstructions instructions, Results results) {
            log.info("Torchie "+torchie.getTorchieId() + " completed command `" + instructions.getCmdDescription() +
                    "` in "+instructions.getExecutionDuration()
                    + " with queue time: "+ instructions.getQueueDuration());
        }
    }
}
