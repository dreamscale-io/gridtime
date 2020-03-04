package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results;
import com.dreamscale.gridtime.core.machine.executor.circuit.*;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.GridSyncLockManager;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.SystemExclusiveJobClaimManager;
import com.dreamscale.gridtime.core.machine.executor.job.CalendarGeneratorJob;
import com.dreamscale.gridtime.core.machine.executor.job.CalendarJobDescriptor;
import com.dreamscale.gridtime.core.machine.executor.monitor.CircuitActivityDashboard;
import com.dreamscale.gridtime.core.machine.executor.monitor.MonitorType;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


@Component
public class SystemWorkPile implements WorkPile {

    @Autowired
    private CircuitActivityDashboard activityDashboard;

    @Autowired
    private GridSyncLockManager gridSyncLockManager;

    @Autowired
    private SystemExclusiveJobClaimManager systemExclusiveJobClaimManager;

    @Autowired
    private TimeService timeService;

    @Autowired
    CalendarGeneratorJob calendarGeneratorJob;


    private final WhatsNextWheel<TickInstructions> whatsNextWheel = new WhatsNextWheel<>();

    private LocalDateTime lastSyncCheck;
    private TickInstructions peekInstruction;

    private Duration syncInterval = Duration.ofMinutes(20);


    private IdeaFlowCircuit calendarCircuit;
    private IdeaFlowCircuit dashboardCircuit;

    @PostConstruct
    private void init() {
        createSystemWorkers();
    }

    private void createSystemWorkers() {
        //create one per process type

        UUID calendarWorkerId = UUID.randomUUID();

        CircuitMonitor calendarProcessMonitor = new CircuitMonitor(ProcessType.Calendar, calendarWorkerId);
        calendarCircuit = new IdeaFlowCircuit(calendarProcessMonitor);

        UUID refreshWorkerId = UUID.randomUUID();

        CircuitMonitor refreshProcessMonitor = new CircuitMonitor(ProcessType.Dashboard, refreshWorkerId);
        dashboardCircuit = new IdeaFlowCircuit(refreshProcessMonitor);

    }

    public void sync() {
        LocalDateTime now = timeService.now();
        if (lastSyncCheck == null || now.isAfter(lastSyncCheck.plus(syncInterval))) {
            lastSyncCheck = now;

            gridSyncLockManager.tryToAcquireSystemJobSyncLock();

            if ( calendarGeneratorJob.hasWorkToDo(now) ) {

                UUID workerId = calendarCircuit.getWorkerId();

                CalendarJobDescriptor jobDescriptor = calendarGeneratorJob.createJobDescriptor(now);
                SystemJobClaim systemJobClaim = systemExclusiveJobClaimManager.claimIfNotRunning(workerId, jobDescriptor);


                if (systemJobClaim != null) {
                    Program calendarProgram = calendarGeneratorJob.createStayAheadProgram(jobDescriptor);

                    calendarCircuit.notifyWhenProgramDone(new SystemJobDoneTrigger(systemJobClaim));
                    calendarCircuit.notifyWhenProgramFails(new SystemJobFailedTrigger(systemJobClaim));
                    calendarCircuit.runProgram(calendarProgram);

                    activityDashboard.addMonitor(MonitorType.SYS_WORKER, calendarCircuit.getWorkerId(), calendarCircuit.getCircuitMonitor());


                    whatsNextWheel.addWorker(workerId, calendarCircuit);

                }
            }

            gridSyncLockManager.releaseSystemJobLock();

        }
    }

    public boolean hasWork() {

        if (peekInstruction == null) {
            peek();
        }
        return peekInstruction != null;
    }

    private void peek() {

        if (peekInstruction == null) {
            peekInstruction = whatsNextWheel.whatsNext();

            while (peekInstruction == null && whatsNextWheel.isNotExhausted()) {

                evictLastWorker();
                peekInstruction = whatsNextWheel.whatsNext();

            }
        }
    }

    public TickInstructions whatsNext() {
       return whatsNextWheel.whatsNext();
    }

    @Override
    public void evictLastWorker() {
        //no-op, workers can't be evicted for now

    }

    @Override
    public int size() {
        return whatsNextWheel.size();
    }

    public void submitWork(ProcessType processType, TickInstructions instruction) {
        if (processType == ProcessType.Calendar) {
            calendarCircuit.scheduleHighPriorityInstruction(instruction);
        }
        if (processType == ProcessType.Dashboard) {
            dashboardCircuit.scheduleHighPriorityInstruction(instruction);
        }
    }

    private class SystemJobDoneTrigger implements NotifyDoneTrigger {

        private final SystemJobClaim systemJobClaim;

        SystemJobDoneTrigger(SystemJobClaim systemJobClaim) {
            this.systemJobClaim = systemJobClaim;
        }

        @Override
        public void notifyWhenDone(TickInstructions instructions, List<Results> results) {
           systemExclusiveJobClaimManager.finishClaim(systemJobClaim);

           activityDashboard.evictMonitor(MonitorType.SYS_WORKER, systemJobClaim.getWorkerId());

            whatsNextWheel.evictWorker(systemJobClaim.getWorkerId());
        }
    }

    private class SystemJobFailedTrigger implements NotifyFailureTrigger {

        private final SystemJobClaim systemJobClaim;

        SystemJobFailedTrigger(SystemJobClaim systemJobClaim) {
            this.systemJobClaim = systemJobClaim;
        }

        @Override
        public void notifyOnFailure(TickInstructions instructions, Exception ex) {
            systemExclusiveJobClaimManager.failClaim(systemJobClaim, ex.getMessage());

            activityDashboard.evictMonitor(MonitorType.SYS_WORKER, systemJobClaim.getWorkerId());

            whatsNextWheel.evictWorker(systemJobClaim.getWorkerId());
        }
    }


}
