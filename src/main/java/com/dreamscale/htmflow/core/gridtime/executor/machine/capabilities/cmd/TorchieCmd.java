package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd;

import com.dreamscale.htmflow.core.gridtime.executor.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.executor.machine.Torchie;
import com.dreamscale.htmflow.core.gridtime.executor.machine.TorchiePoolExecutor;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.circuit.NotifyTrigger;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.Results;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.CmdType;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.TrackSetName;
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


    public void gotoTile(ZoomLevel zoom, LocalDateTime tileTime) {
        TileInstructions instructions = torchie.getInstructionsBuilder().gotoTile(zoom, tileTime);
        runInstructionAndWaitTilDone(instructions);
    }

    public void nextTile() {
        TileInstructions instructions = torchie.getInstructionsBuilder().nextTile();
        runInstructionAndWaitTilDone(instructions);
    }

    public Results playTile() {
        TileInstructions instructions = torchie.getInstructionsBuilder().playTile();
        runInstructionAndWaitTilDone(instructions);

        return instructions.getOutputResults();
    }

    public Results playTrack(TrackSetName trackSetName) {
        TileInstructions instructions = torchie.getInstructionsBuilder().playTrack(trackSetName);
        runInstructionAndWaitTilDone(instructions);

        return instructions.getOutputResults();
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
