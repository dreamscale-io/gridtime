package com.dreamscale.gridtime.core.machine.executor.circuit.wires;

import com.dreamscale.gridtime.core.domain.work.*;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.GridSyncLockManager;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class AggregateWorkToDoQueueWire implements Wire {

    private static final Duration DELAY_BEFORE_PROCESSING_PARTIAL_WORK = Duration.ofMinutes(30);

    @Autowired
    private GridSyncLockManager gridSyncLockManager;

    @Autowired
    private WorkItemToAggregateRepository workItemToAggregateRepository;

    @Autowired
    private WorkReadyByTeamViewRepository workReadyByTeamViewRepository;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private GridClock gridClock;

    @Override
    @Transactional
    public void pushAll(List<TileStreamEvent> tileStreamEvents) {
        for (TileStreamEvent tileStreamEvent : tileStreamEvents) {
            push(tileStreamEvent);
        }
    }

    @Override
    @Transactional
    public void push(TileStreamEvent event) {

        log.info("Pushing event into WorkToDo queue: "+event.gridTime);

        Long tileSeq = calendarService.lookupTileSequenceNumber(event.getGridTime());

        WorkItemToAggregateEntity workItem = new WorkItemToAggregateEntity();
        workItem.setId(UUID.randomUUID());
        workItem.setEventTime(gridClock.now());
        workItem.setProcessingState(ProcessingState.Ready);
        workItem.setZoomLevel(event.getGridTime().getZoomLevel());
        workItem.setTileSeq(tileSeq);
        workItem.setGridTime(event.getGridTime().toDisplayString());
        workItem.setSourceTorchieId(event.getTorchieId());
        workItem.setTeamId(event.getTeamId());
        workItem.setWorkToDoType(WorkToDoType.AggregateToTeam);

        workItemToAggregateRepository.save(workItem);

    }


    @Transactional
    @Override
    public AggregateStreamEvent pullNext(UUID workerId) {

        AggregateStreamEvent nextEvent = null;

        gridSyncLockManager.tryToAcquirePlexerSyncLock();

        WorkToDo workToDo = getNextWorkToDo();

        if (workToDo != null) {
            GeometryClock.GridTimeSequence sequence = calendarService.lookupGridTimeSequence(workToDo.getZoomLevel(), workToDo.getTileSeq());

            nextEvent = new AggregateStreamEvent(workToDo.getTeamId(), sequence.getGridTime(), workToDo.getWorkToDoType());

            workItemToAggregateRepository.updateInProgress(workerId.toString(), workToDo.getTeamId().toString(), workToDo.getZoomLevel().toString(), workToDo.getTileSeq());
        }

        gridSyncLockManager.releasePlexerSyncLock();


        return nextEvent;
    }

    private WorkToDo getNextWorkToDo() {

        //first check whether there is stuff ready at the team level

        WorkReadyByTeamViewEntity workItem = workReadyByTeamViewRepository.findOldestTeamCompleteWorkItem();

        if (workItem != null) {
            log.debug(workItem.toString());

            return toWorkToDo(workItem);
        }

        //if there's no complete stuff, then we can also process incomplete stuff after a delay

        LocalDateTime partialWorkReadyDate = gridClock.now().minus(DELAY_BEFORE_PROCESSING_PARTIAL_WORK);

         workItem = workReadyByTeamViewRepository.findOldestPartialTeamWorkItemOlderThan(Timestamp.valueOf(partialWorkReadyDate));

         if (workItem != null) {
             return toWorkToDo(workItem);
         }

         return null;

    }

    private WorkToDo toWorkToDo(WorkReadyByTeamViewEntity workItem) {
        return new WorkToDo(workItem.getWorkId().getTeamId(), workItem.getZoomLevel(), workItem.getTileSeq(), workItem.getWorkToDoType());
    }


    @Transactional
    @Override
    public void markDone(UUID workerId) {
        workItemToAggregateRepository.finishInProgressWorkItems(workerId.toString());
    }


    @Override
    public int getQueueDepth() {
        long queueDepth = workReadyByTeamViewRepository.count();
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
