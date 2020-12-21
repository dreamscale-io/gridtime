package com.dreamscale.gridtime.core.machine.capabilities.cmd;


import com.dreamscale.gridtime.api.grid.Results;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyDoneTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyFailureTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Notifier;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class SyncCmd {

    private final NotifyDoneTrigger NOTIFY_WHEN_DONE;
    private final NotifyFailureTrigger NOTIFY_WHEN_FAILED;

    private static final int MAX_WAIT_LOOPS = 30;

    private boolean syncCommandInProgress;

    private boolean lastCommandSuccess = true;
    private Exception lastException;

    protected SyncCmd() {
        syncCommandInProgress = false;

        NOTIFY_WHEN_DONE = new UpdateCommandDoneTrigger();
        NOTIFY_WHEN_FAILED = new UpdateCommandFailedTrigger();
    }

    protected void startCommandInProgress() {
        syncCommandInProgress = true;
    }

    protected void configureNotify(Notifier notifier) {
        notifier.notifyOnDone(NOTIFY_WHEN_DONE);
        notifier.notifyOnFail(NOTIFY_WHEN_FAILED);
    }

    protected NotifyDoneTrigger getNotifyDoneTrigger() {
        return NOTIFY_WHEN_DONE;
    }

    protected NotifyFailureTrigger getNotifyFailedTrigger() {
        return NOTIFY_WHEN_FAILED;
    }

    protected void waitForCommandToFinish() {
        int waitLoopCounter = MAX_WAIT_LOOPS;
        try {
            while (syncCommandInProgress && waitLoopCounter > 0) {
                Thread.sleep(1000);
                waitLoopCounter--;
            }
            if (waitLoopCounter == 0) {
                log.error("Wait loop count exceeded");
            }
            if (lastException != null || !lastCommandSuccess) {
                throw new RuntimeException("System command aborted or failed ", lastException);
            }

        } catch (InterruptedException ex) {
            log.error("Interrupted", ex);
        }

    }

    private class UpdateCommandDoneTrigger implements NotifyDoneTrigger {
        @Override
        public void notifyWhenDone(TickInstructions instructions, List<Results> results) {
            log.debug("System command finished successfully");
            syncCommandInProgress = false;
            lastException = null;
            lastCommandSuccess = true;
        }
    }

    private class UpdateCommandFailedTrigger implements NotifyFailureTrigger {
        @Override
        public void notifyOnAbortOrFailure(TickInstructions instructions, Exception ex) {
            log.error("System command failed. ", ex);
            syncCommandInProgress = false;
            lastException = ex;
            lastCommandSuccess = false;
        }
    }
}
