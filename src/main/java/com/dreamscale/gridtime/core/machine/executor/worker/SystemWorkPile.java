package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.api.grid.Results;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.SyncCmd;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.SystemCmd;
import com.dreamscale.gridtime.core.machine.executor.circuit.*;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.NoOpInstruction;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.GridSyncLockManager;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.SystemExclusiveJobClaimManager;
import com.dreamscale.gridtime.core.machine.executor.dashboard.ProcessStatus;
import com.dreamscale.gridtime.core.machine.executor.job.CalendarGeneratorJob;
import com.dreamscale.gridtime.core.machine.executor.job.CalendarJobDescriptor;
import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard;
import com.dreamscale.gridtime.core.machine.executor.dashboard.MonitorType;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Component
public class SystemWorkPile implements WorkPile {

    @Autowired
    private CircuitActivityDashboard activityDashboard;

    @Autowired
    private GridSyncLockManager gridSyncLockManager;

    @Autowired
    private SystemExclusiveJobClaimManager systemExclusiveJobClaimManager;

    @Autowired
    private GridClock gridClock;

    @Autowired
    CalendarGeneratorJob calendarGeneratorJob;


    private final WhatsNextWheel whatsNextWheel = new WhatsNextWheel();

    private LocalDateTime lastSyncCheck;
    private TickInstructions peekInstruction;

    private Duration syncInterval = Duration.ofMinutes(5);

    private IdeaFlowCircuit calendarCircuit;
    private IdeaFlowCircuit dashboardCircuit;

    private boolean paused = false;
    private boolean autorun = true;

    private int daysToKeepAhead = 365; //block, year, tiles aren't saved until the end,


    @PostConstruct
    private void init() {
        createSystemWorkers();
    }

    private void createSystemWorkers() {
        //create one per process type, these never get evicted

        UUID calendarWorkerId = UUID.randomUUID();

        CircuitMonitor calendarProcessMonitor = new CircuitMonitor(ProcessType.Calendar, calendarWorkerId);
        calendarCircuit = new IdeaFlowCircuit(calendarProcessMonitor);

        activityDashboard.addMonitor(MonitorType.SYSTEM_CALENDAR, calendarCircuit.getWorkerId(), calendarCircuit.getCircuitMonitor());
        whatsNextWheel.addWorker(calendarWorkerId, calendarCircuit);

        UUID dashboardWorkerId = UUID.randomUUID();

        CircuitMonitor dashboardProcessMonitor = new CircuitMonitor(ProcessType.Dashboard, dashboardWorkerId);
        dashboardCircuit = new IdeaFlowCircuit(dashboardProcessMonitor);

        activityDashboard.addMonitor(MonitorType.SYSTEM_DASHBOARD, dashboardCircuit.getWorkerId(), dashboardCircuit.getCircuitMonitor());
        whatsNextWheel.addWorker(dashboardWorkerId, dashboardCircuit);

    }

    public void configureDaysToKeepAhead(int days) {
        this.daysToKeepAhead = days;
    }

    @Transactional
    public void sync() {
        LocalDateTime now = gridClock.now();
        if (lastSyncCheck == null || now.isAfter(lastSyncCheck.plus(syncInterval))) {
            lastSyncCheck = now;

            log.info("synchronizing system work");

            gridSyncLockManager.tryToAcquireSystemJobSyncLock();

            try {
                updateHeartbeatOfRunningPrograms(now);
                if (autorun) {
                    spinUpCalendarProgramIfNeeded(now);
                }
            } finally {
                gridSyncLockManager.releaseSystemJobSyncLock();
            }
        }
    }


    @Override
    public void reset() {
        shutdown();
        createSystemWorkers();

        lastSyncCheck = null;
        peekInstruction = null;
        autorun = true;

        paused = false;
    }

    @Override
    public void shutdown() {
        calendarCircuit.abortAndClearProgram();
        dashboardCircuit.abortAndClearProgram();

        whatsNextWheel.clear();

        peekInstruction = null;
    }

    private void updateHeartbeatOfRunningPrograms(LocalDateTime now) {

        if (calendarCircuit.isProgramRunning()) {
            systemExclusiveJobClaimManager.updateHeartbeat(now, calendarCircuit.getWorkerId());
        }
    }

    private void spinUpCalendarProgramIfNeeded(LocalDateTime now) {

        CalendarJobDescriptor jobDescriptor = calendarGeneratorJob.createJobDescriptor(now, daysToKeepAhead);

        runCalendarProgram(now, jobDescriptor);
    }

    private synchronized void runCalendarProgram(LocalDateTime now, CalendarJobDescriptor jobDescriptor) {
        if ( !calendarCircuit.isProgramRunning() && calendarGeneratorJob.hasWorkToDo(jobDescriptor) ) {

            UUID workerId = calendarCircuit.getWorkerId();

            SystemJobClaim systemJobClaim = systemExclusiveJobClaimManager.claimIfNotRunning(now, workerId, jobDescriptor);

            if (systemJobClaim != null) {
                Program calendarProgram = calendarGeneratorJob.createStayAheadProgram(jobDescriptor);

                log.info("Starting program {}", jobDescriptor.getJobType().name());
                activityDashboard.updateProcessStatus(calendarCircuit.getWorkerId(), ProcessStatus.ACTIVE);

                calendarCircuit.notifyOnDone(new SystemJobDoneTrigger(systemJobClaim));
                calendarCircuit.notifyOnFail(new SystemJobFailedTrigger(systemJobClaim));

                calendarCircuit.runProgram(calendarProgram);
            } else {
                activityDashboard.updateProcessStatus(calendarCircuit.getWorkerId(), ProcessStatus.BLOCKED);
            }
        } else {
            log.warn("Calendar program has no more work to do.");
            activityDashboard.updateProcessStatus(calendarCircuit.getWorkerId(), ProcessStatus.IDLE);
        }
    }

    public boolean hasWork() {

        peek();

        return peekInstruction != null;
    }

    private void peek() {

        if (peekInstruction == null) {

            for (int i = 0; i < whatsNextWheel.size(); i++) {
                peekInstruction = whatsNextWheel.whatsNext();

                if (peekInstruction instanceof NoOpInstruction) {
                    log.info("no-op");
                    peekInstruction = null;
                }

                if (peekInstruction != null) {
                    break;
                }
            }
        }
    }



    public TickInstructions whatsNext() {
        if (paused) return null;

        peek();

        TickInstructions nextInstruction = peekInstruction;
        peekInstruction = null;

        return nextInstruction;
    }

    @Override
    public int size() {
        return whatsNextWheel.size();
    }

    public void submitWork(ProcessType processType, TickInstructions instruction) {
        log.debug("Submitting work for {}", processType.name());
        if (processType == ProcessType.Calendar) {
            calendarCircuit.scheduleHighPriorityInstruction(instruction);
        }
        if (processType == ProcessType.Dashboard) {
            dashboardCircuit.scheduleHighPriorityInstruction(instruction);
        }
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @Override
    public void setAutorun(boolean isAutorun) {
        this.autorun = isAutorun;
    }

    public SystemCmd getSystemCmd() {
        return new InternalSystemCmd();
    }


    private class InternalSystemCmd extends SyncCmd implements SystemCmd {

        public void runCalendarUntil(LocalDateTime runUntilDate) {
            startCommandInProgress();

            LocalDateTime now = gridClock.now();
            CalendarJobDescriptor jobDescriptor = calendarGeneratorJob.createJobDescriptor(runUntilDate);

            if (!calendarCircuit.isProgramRunning()) {
                configureNotify(calendarCircuit);

                runCalendarProgram(now, jobDescriptor);
            }
            waitForCommandToFinish();
        }

        public void abortCalendarProgram() {
            if (calendarCircuit.isProgramRunning()) {
                calendarCircuit.abortAndClearProgram();
            }
        }

    }

    private class SystemJobDoneTrigger implements NotifyDoneTrigger {

        private final SystemJobClaim systemJobClaim;

        SystemJobDoneTrigger(SystemJobClaim systemJobClaim) {
            this.systemJobClaim = systemJobClaim;
        }

        @Override
        public void notifyWhenDone(TickInstructions instructions, List<Results> results) {
            log.info("Finished program {}", systemJobClaim.getJobType().name());
           systemExclusiveJobClaimManager.finishClaim(systemJobClaim);

           activityDashboard.updateProcessStatus(calendarCircuit.getWorkerId(), ProcessStatus.IDLE);
        }
    }

    private class SystemJobFailedTrigger implements NotifyFailureTrigger {

        private final SystemJobClaim systemJobClaim;

        SystemJobFailedTrigger(SystemJobClaim systemJobClaim) {
            this.systemJobClaim = systemJobClaim;
        }

        @Override
        public void notifyOnAbortOrFailure(TickInstructions instructions, Exception ex) {
            if (ex != null) {
                log.error("Failing program "+systemJobClaim.getJobType().name(), ex);
                systemExclusiveJobClaimManager.failClaim(systemJobClaim, ex.getMessage());

                activityDashboard.updateProcessStatus(calendarCircuit.getWorkerId(), ProcessStatus.FAILED);
            } else {
                log.warn("Aborting program {}", systemJobClaim.getJobType().name());
                systemExclusiveJobClaimManager.abortClaim(systemJobClaim);

                activityDashboard.updateProcessStatus(calendarCircuit.getWorkerId(), ProcessStatus.IDLE);
            }
        }
    }


}
