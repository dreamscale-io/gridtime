package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results;
import com.dreamscale.gridtime.core.machine.executor.circuit.ProcessType;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.IdeaFlowCircuit;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.GridSyncLockManager;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.SystemExclusiveJobClaimManager;
import com.dreamscale.gridtime.core.machine.executor.job.CalendarGeneratorJob;
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
    private CircuitActivityDashboard circuitActivityDashboard;

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


    private IdeaFlowCircuit calendarProcessCircuit;
    private IdeaFlowCircuit dashboardProcessCircuit;

    @PostConstruct
    private void init() {
        createSystemWorkers();
    }

    private void createSystemWorkers() {
        //create one per process type

        UUID calendarWorkerId = UUID.randomUUID();

        CircuitMonitor calendarProcessMonitor = new CircuitMonitor(ProcessType.Calendar, calendarWorkerId);
        calendarProcessCircuit = new IdeaFlowCircuit(calendarProcessMonitor);

        UUID refreshWorkerId = UUID.randomUUID();

        CircuitMonitor refreshProcessMonitor = new CircuitMonitor(ProcessType.Dashboard, refreshWorkerId);
        dashboardProcessCircuit = new IdeaFlowCircuit(refreshProcessMonitor);

    }

    public void sync() {
        LocalDateTime now = timeService.now();
        if (lastSyncCheck == null || now.isAfter(lastSyncCheck.plus(syncInterval))) {
            lastSyncCheck = now;

            //so if I have a job that is running for an hour, how long since my last update?

            //check the circuit monitors for each job...

            //in a loop, get the workers, check if they're moving...

            //what if the process died and I've got a stale thing in the DB?


            gridSyncLockManager.tryToAcquireSystemJobSyncLock();


            systemExclusiveJobClaimManager.cleanUpStaleClaims(now);


            //see if I need to be doing any calendar generation, spin up a job if needed.

            if ( calendarGeneratorJob.hasWorkToDo(now) ) {

                UUID workerId = UUID.randomUUID();

                SystemJobClaim systemJobClaim = systemExclusiveJobClaimManager.claimIfNotRunning(workerId, calendarGeneratorJob.createJobClaim(now));

                if (systemJobClaim != null) {
                    Program calendarProgram = calendarGeneratorJob.createStayAheadProgram(now);

                    calendarProcessCircuit.notifyWhenProgramDone(new SystemJobDoneTrigger(systemJobClaim));
                    calendarProcessCircuit.runProgram(calendarProgram);

                    CircuitMonitor circuitMonitor = new CircuitMonitor(ProcessType.Calendar, workerId);
                    IdeaFlowCircuit circuit = new IdeaFlowCircuit(circuitMonitor, calendarProgram);



                    circuitActivityDashboard.addMonitor(MonitorType.SYSTEM_WORKER, workerId, circuitMonitor);

                    whatsNextWheel.addWorker(workerId, circuit);

                }
            }

            gridSyncLockManager.releaseSystemJobLock();

        }
    }

    //TODO what if this job fails, how does it recover?

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
            calendarProcessCircuit.scheduleHighPriorityInstruction(instruction);
        }
        if (processType == ProcessType.Dashboard) {
            dashboardProcessCircuit.scheduleHighPriorityInstruction(instruction);
        }
    }

    private class SystemJobDoneTrigger implements NotifyTrigger {

        private final SystemJobClaim systemJobClaim;

        SystemJobDoneTrigger(SystemJobClaim systemJobClaim) {
            this.systemJobClaim = systemJobClaim;
        }

        @Override
        public void notifyWhenDone(TickInstructions instructions, List<Results> results) {
           systemExclusiveJobClaimManager.finishClaim(systemJobClaim);

           circuitActivityDashboard.evictMonitor(MonitorType.SYSTEM_WORKER, systemJobClaim.getWorkerId());

            whatsNextWheel.evictWorker(systemJobClaim.getWorkerId());
        }
    }

}
