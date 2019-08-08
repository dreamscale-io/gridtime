package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires;

import com.dreamscale.htmflow.core.domain.work.ProcessingState;
import com.dreamscale.htmflow.core.domain.work.WorkToDoType;
import com.dreamscale.htmflow.core.domain.work.WorkItemToAggregateEntity;
import com.dreamscale.htmflow.core.domain.work.WorkItemToAggregateRepository;
import com.dreamscale.htmflow.core.gridtime.exception.UnableToLockException;
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.htmflow.core.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class QueuedWorkToDoWire implements Wire {

    private static final Long WORKER_WRITE_LOCK = Long.MAX_VALUE;

    @Autowired
    private WorkItemToAggregateRepository workItemToAggregateRepository;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private TimeService timeService;

    @Override
    public void publishAll(List<TileStreamEvent> tileStreamEvents) {

    }

    @Override
    public void publish(TileStreamEvent event) {

        Long tileSeq = calendarService.lookupTileSequenceNumber(event.getGridTime());

        WorkItemToAggregateEntity workItem = new WorkItemToAggregateEntity();
        workItem.setId(UUID.randomUUID());
        workItem.setEventTime(timeService.now());
        workItem.setProcessingState(ProcessingState.Ready);
        workItem.setZoomLevel(event.getGridTime().getZoomLevel());
        workItem.setTileSeq(tileSeq);
        workItem.setGridTime(event.getGridTime().toDisplayString());
        workItem.setSourceTorchieId(event.getTorchieId());
        workItem.setTeamId(event.getTeamId());
        workItem.setWorkToDoType(WorkToDoType.AggregateToTeam);

        workItemToAggregateRepository.save(workItem);
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public AggregateStreamEvent pullNext(UUID workerId) {

        AggregateStreamEvent nextEvent = null;

        tryToAcquireWorkerLock(WORKER_WRITE_LOCK);

        WorkItemToAggregateEntity workItem = workItemToAggregateRepository.findOldestReadyWorkItem();

        if (workItem != null) {
            GeometryClock.GridTimeSequence sequence = calendarService.lookupGridTimeSequence(workItem.getZoomLevel(), workItem.getTileSeq());

            nextEvent = new AggregateStreamEvent(workItem.getTeamId(), sequence.getGridTime(), workItem.getWorkToDoType());

            workItemToAggregateRepository.updateInProgress(workerId.toString(), workItem.getTeamId().toString(), workItem.getZoomLevel().toString(), workItem.getTileSeq());
        }

        releaseWorkerLock(WORKER_WRITE_LOCK);


        //lock table pull, Long.Max is always the global lock

        //then get the latest event, and update the inprogress status of all the things pulled
        //add an in progress timestamp to the table, so we can pick up stuff that dies

        //then finally release the lock

        //map the entry to an aggregate stream event

        return nextEvent;
    }

    @Override
    public void markDone(UUID workerId) {

        workItemToAggregateRepository.finishInProgressWorkItems(workerId.toString());
    }


    private void releaseWorkerLock(Long lockNumber) {
        boolean release = workItemToAggregateRepository.releaseLock(lockNumber);
    }

    private void tryToAcquireWorkerLock(Long lockNumber) {

        boolean lockAcquired = false;
        int tries = 0;

        while (!lockAcquired && tries < 10) {
            lockAcquired = workItemToAggregateRepository.tryToAcquireLock(lockNumber);
            tries++;
        }

        if (!lockAcquired) {
            throw new UnableToLockException("Unable to acquire worker lock after 10 tries");
        }

    }

    @Override
    public int getQueueDepth() {
        return 0;
    }
}
