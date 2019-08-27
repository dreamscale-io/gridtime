package com.dreamscale.gridtime.core.machine.executor.circuit.wires;

import com.dreamscale.gridtime.core.domain.work.*;
import com.dreamscale.gridtime.core.exception.UnableToLockException;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.gridtime.core.service.TimeService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class WorkToDoQueueWire implements Wire {

    private static final Long WORKER_WRITE_LOCK = Long.MAX_VALUE;

    private static final Duration DELAY_BEFORE_PROCESSING_PARTIAL_WORK = Duration.ofMinutes(30);


    @Autowired
    private WorkItemToAggregateRepository workItemToAggregateRepository;

    @Autowired
    private WorkReadyByTeamViewRepository workReadyByTeamViewRepository;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private TimeService timeService;

    @Override
    public void pushAll(List<TileStreamEvent> tileStreamEvents) {
        for (TileStreamEvent tileStreamEvent : tileStreamEvents) {
            push(tileStreamEvent);
        }
    }

    @Override
    public void push(TileStreamEvent event) {

        log.info("Pushing event into WorkToDo queue: "+event.gridTime);

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
    public AggregateStreamEvent pullNext(UUID workerId) {

        AggregateStreamEvent nextEvent = null;

        tryToAcquireWorkerLock(WORKER_WRITE_LOCK);

        WorkToDo workToDo = getNextWorkToDo();

        if (workToDo != null) {
            GeometryClock.GridTimeSequence sequence = calendarService.lookupGridTimeSequence(workToDo.getZoomLevel(), workToDo.getTileSeq());

            nextEvent = new AggregateStreamEvent(workToDo.getTeamId(), sequence.getGridTime(), workToDo.getWorkToDoType());

            workItemToAggregateRepository.updateInProgress(workerId.toString(), workToDo.getTeamId().toString(), workToDo.getZoomLevel().toString(), workToDo.getTileSeq());
        }

        releaseWorkerLock(WORKER_WRITE_LOCK);


        //lock table pull, Long.Max is always the global lock

        //then get the latest event, and update the inprogress status of all the things pulled
        //add an in progress timestamp to the table, so we can pick up stuff that dies

        //then finally release the lock

        //map the entry to an aggregate stream event

        return nextEvent;
    }

    private WorkToDo getNextWorkToDo() {
        log.info("getNextWorkToDo");

        //first check whether there is stuff ready at the team level

        WorkReadyByTeamViewEntity workItem = workReadyByTeamViewRepository.findOldestTeamCompleteWorkItem();

        if (workItem != null) {
            log.debug(workItem.toString());

            return toWorkToDo(workItem);
        }

        //if there's no complete stuff, then we can also process incomplete stuff after a delay

        LocalDateTime partialWorkReadyDate = timeService.now().minus(DELAY_BEFORE_PROCESSING_PARTIAL_WORK);

         workItem = workReadyByTeamViewRepository.findOldestPartialTeamWorkItemOlderThan(Timestamp.valueOf(partialWorkReadyDate));

         if (workItem != null) {
             return toWorkToDo(workItem);
         }

         return null;

    }

    private WorkToDo toWorkToDo(WorkReadyByTeamViewEntity workItem) {
        return new WorkToDo(workItem.getWorkId().getTeamId(), workItem.getZoomLevel(), workItem.getTileSeq(), workItem.getWorkToDoType());
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
        long queueDepth = workReadyByTeamViewRepository.count();

        log.info("Active queue depth: "+queueDepth);
        return (int)queueDepth;
    }

    @AllArgsConstructor
    @Getter
    private class WorkToDo {
        UUID teamId;
        ZoomLevel zoomLevel;
        Long tileSeq;
        WorkToDoType workToDoType;
    }
}
