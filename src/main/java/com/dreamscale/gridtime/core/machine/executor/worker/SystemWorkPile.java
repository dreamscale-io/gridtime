package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.IdeaFlowCircuit;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.GridtimeLockManager;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.SystemJobClaimManager;
import com.dreamscale.gridtime.core.machine.executor.job.CalendarGeneratorJob;
import com.dreamscale.gridtime.core.machine.executor.monitor.CircuitActivityDashboard;
import com.dreamscale.gridtime.core.machine.executor.monitor.MonitorType;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


@Component
public class SystemWorkPile implements WorkPile {

    @Autowired
    private CircuitActivityDashboard circuitActivityDashboard;

    @Autowired
    private GridtimeLockManager gridtimeLockManager;

    @Autowired
    private SystemJobClaimManager systemJobClaimManager;

    @Autowired
    private TimeService timeService;

    @Autowired
    CalendarGeneratorJob calendarGeneratorJob;


    private final WhatsNextWheel<TickInstructions> whatsNextWheel = new WhatsNextWheel<>();

    private LocalDateTime lastSyncCheck;
    private TickInstructions peekInstruction;

    private Duration syncInterval = Duration.ofMinutes(20);

    public void sync() {
        LocalDateTime now = timeService.now();
        if (lastSyncCheck == null || now.isAfter(lastSyncCheck.plus(syncInterval))) {
            lastSyncCheck = now;

            systemJobClaimManager.cleanUpStaleClaims(now);

            //so if I have a job that is running for an hour, how long since my last update?

            //check the circuit monitors for each job...

            //in a loop, get the workers, check if they're moving...

            //what if the process died and I've got a stale thing in the DB?


            gridtimeLockManager.tryToAcquireSystemJobLock();

            //see if I need to be doing any calendar generation, spin up a job if needed.

            if ( calendarGeneratorJob.hasWorkToDo(now) ) {

                UUID workerId = UUID.randomUUID();

                WorkerClaim workerClaim = systemJobClaimManager.claimIfNotRunning(workerId, calendarGeneratorJob.getJobClaim(now));

                if (workerClaim != null) {
                    Program calendarProgram = calendarGeneratorJob.createStayAheadProgram(now);

                    CircuitMonitor circuitMonitor = new CircuitMonitor(workerId);
                    IdeaFlowCircuit circuit = new IdeaFlowCircuit(circuitMonitor, calendarProgram);

                    circuit.notifyWhenProgramDone(new SystemJobDoneTrigger(workerClaim));

                    circuitActivityDashboard.addMonitor(MonitorType.SYSTEM_WORKER, workerId, circuitMonitor);

                    whatsNextWheel.addWorker(workerId, circuit);

                }
            }

            gridtimeLockManager.releaseSystemJobLock();

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


    private class SystemJobDoneTrigger implements NotifyTrigger {

        private final WorkerClaim workerClaim;

        SystemJobDoneTrigger(WorkerClaim workerClaim) {
            this.workerClaim = workerClaim;
        }

        @Override
        public void notifyWhenDone(TickInstructions instructions, List<Results> results) {
           systemJobClaimManager.finishClaim(workerClaim);

           circuitActivityDashboard.evictMonitor(MonitorType.SYSTEM_WORKER, workerClaim.getWorkerId());

            whatsNextWheel.evictWorker(workerClaim.getWorkerId());
        }
    }

}
