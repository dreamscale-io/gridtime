package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.IdeaFlowCircuit;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.GridtimeLockManager;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.SystemJobClaimManager;
import com.dreamscale.gridtime.core.machine.executor.job.CalendarGeneratorJob;
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
    private GridtimeLockManager gridtimeLockManager;

    @Autowired
    private SystemJobClaimManager systemJobClaimManager;

    @Autowired
    private TimeService timeService;

    @Autowired
    CalendarGeneratorJob calendarGeneratorJob;


    private final WhatsNextWheel<TickInstructions> whatsNextWheel = new WhatsNextWheel<>();

    List<WorkerClaim> activeClaims = new ArrayList<>();

    private LocalDateTime lastSyncCheck;
    private TickInstructions peekInstruction;

    private Duration syncInterval = Duration.ofMinutes(20);
    private Duration expireWhenStaleMoreThan = Duration.ofMinutes(60);

    public void sync() {
        LocalDateTime now = timeService.now();
        if (lastSyncCheck == null || now.isAfter(lastSyncCheck.plus(syncInterval))) {
            lastSyncCheck = now;

            systemJobClaimManager.cleanUpStaleClaims(now);

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

                    activeClaims.add(workerClaim);

                    whatsNextWheel.addWorker(workerId, circuit);

                }
            }

            gridtimeLockManager.releaseSystemJobLock();

        }
    }

    //TODO what if this job fails, how does it recover?

    //TODO how do I monitor this job while it's running?

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

                whatsNextWheel.evictLastWorker();
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
           activeClaims.remove(workerClaim);

           whatsNextWheel.evictWorker(workerClaim.getWorkerId());
        }
    }

}
