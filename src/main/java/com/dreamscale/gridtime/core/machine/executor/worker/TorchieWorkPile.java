package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity;
import com.dreamscale.gridtime.core.domain.flow.FlowActivityRepository;
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity;
import com.dreamscale.gridtime.core.domain.journal.IntentionRepository;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorEntity;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorRepository;
import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.TorchieFactory;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.GridSyncLockManager;
import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard;
import com.dreamscale.gridtime.core.machine.executor.dashboard.MonitorType;
import com.dreamscale.gridtime.core.service.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class TorchieWorkPile implements WorkPile {

    @Autowired
    private CircuitActivityDashboard circuitActivityDashboard;

    @Autowired
    private GridSyncLockManager gridSyncLockManager;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private TorchieFeedCursorRepository torchieFeedCursorRepository;

    @Autowired
    private IntentionRepository intentionRepository;

    @Autowired
    private FlowActivityRepository flowActivityRepository;

    @Autowired
    private TorchieFactory torchieFactory;

    private LocalDateTime lastSyncCheck;
    private TickInstructions peekInstruction;

    private final WhatsNextWheel<TickInstructions> whatsNextWheel = new WhatsNextWheel<>();

    private Duration syncInterval = Duration.ofMinutes(20);
    private Duration expireWhenStaleMoreThan = Duration.ofMinutes(60);

    private static final int MAX_TORCHIES = 10;

    public void sync() {
        LocalDateTime now = gridClock.now();
        if (lastSyncCheck == null || now.isAfter(lastSyncCheck.plus(syncInterval))) {
            lastSyncCheck = now;

            gridSyncLockManager.tryToAcquireTorchieSyncLock();

            initializeMissingTorchies(now);
            claimTorchiesReadyForProcessing();
            expireZombieTorchies();

            gridSyncLockManager.releaseTorchieSyncLock();
        }
    }

    public TorchieCmd getTorchieCmd(UUID torchieId) {

        Torchie torchie = (Torchie) whatsNextWheel.getWorker(torchieId);

        if (torchie == null) {
            torchie = loadTorchie(torchieId);
        }

        return new TorchieCmd(whatsNextWheel, torchie);
    }

    private void claimTorchiesReadyForProcessing() {
        int claimUpTo = MAX_TORCHIES - whatsNextWheel.size();
        List<Torchie> torchies = claimUpTo(claimUpTo);

        for (Torchie torchie : torchies) {
            whatsNextWheel.addWorker(torchie.getTorchieId(), torchie);
            circuitActivityDashboard.addMonitor(MonitorType.TORCHIE_WORKER, torchie.getTorchieId(), torchie.getCircuitMonitor());
        }
    }

    private void initializeMissingTorchies(LocalDateTime now) {
        List<TorchieFeedCursorEntity> missingTorchies = torchieFeedCursorRepository.selectMissingTorchies();
        List<TorchieFeedCursorEntity> readyTorchies = new ArrayList<>();

        for (TorchieFeedCursorEntity torchieCursor : missingTorchies) {

            LocalDateTime firstTilePosition = findFirstTilePosition(torchieCursor.getTorchieId());
            LocalDateTime lastPublishedDataPosition = findLastPublishedData(torchieCursor.getTorchieId());

            if (firstTilePosition != null && lastPublishedDataPosition != null) {
                torchieCursor.setFirstTilePosition(firstTilePosition);
                torchieCursor.setLastPublishedDataCursor(lastPublishedDataPosition);
                torchieCursor.setNextWaitUntilCursor(firstTilePosition.plus(ZoomLevel.TWENTY.getDuration()));
                torchieCursor.setLastClaimUpdate(now);
                readyTorchies.add(torchieCursor);
            } else {
                log.warn("Skipping torchie feed: "+torchieCursor.getTorchieId() + ", waiting for data");
            }
        }

        torchieFeedCursorRepository.save(readyTorchies);
    }

    private List<Torchie> claimUpTo(int limit) {

        List<TorchieFeedCursorEntity> unclaimedTorchies = torchieFeedCursorRepository.findUnclaimedTorchies(limit);

        List<Torchie> claimedTorchies = new ArrayList<>();

        for (TorchieFeedCursorEntity cursor : unclaimedTorchies) {
            Torchie torchie = loadTorchie(cursor);

            claimedTorchies.add(torchie);
        }

        return claimedTorchies;
    }

    private Torchie loadTorchie(UUID torchieId) {
        TorchieFeedCursorEntity cursor = torchieFeedCursorRepository.findOne(torchieId);

        return loadTorchie(cursor);
    }

    private Torchie loadTorchie(TorchieFeedCursorEntity cursor) {
        return torchieFactory.wireUpMemberTorchie(cursor.getTeamId(), cursor.getTorchieId(), getStartPosition(cursor));
    }


    private LocalDateTime getStartPosition(TorchieFeedCursorEntity cursor) {

        LocalDateTime startPosition = null;

        if (cursor.getLastTileProcessedCursor() != null) {
            startPosition = cursor.getLastTileProcessedCursor().plus(ZoomLevel.TWENTY.getDuration());
        } else {
            startPosition = cursor.getFirstTilePosition();
        }

        return startPosition;
    }


    private LocalDateTime findLastPublishedData(UUID torchieId) {
        LocalDateTime lastPublishedData = null;

        FlowActivityEntity flowActivityEntity = flowActivityRepository.findFirst1ByMemberIdOrderByEndDesc(torchieId);

        if (flowActivityEntity != null) {
            lastPublishedData = flowActivityEntity.getEnd();
        }
        return lastPublishedData;
    }

    private LocalDateTime findFirstTilePosition(UUID torchieId) {
        LocalDateTime firstTilePosition = null;

        IntentionEntity intentionEntity = intentionRepository.findFirstByMemberId(torchieId);

        if (intentionEntity != null) {
            firstTilePosition = GeometryClock.roundDownToNearestTwenty(intentionEntity.getPosition());
        }

        return firstTilePosition;

    }


    public void expire(UUID torchieId) {
        torchieFeedCursorRepository.expire(torchieId);
    }


    public void expireZombieTorchies() {

        LocalDateTime expireBeforeDate = gridClock.now().minus(expireWhenStaleMoreThan);

        torchieFeedCursorRepository.expireZombieTorchies(Timestamp.valueOf(expireBeforeDate));

        //expire torchies that dont seem to be running anymore so they can be reclaimed
    }

    public boolean hasWork() {
        if (peekInstruction == null) {
            peek();
        }
        return peekInstruction != null;
    }

    @Override
    public void evictLastWorker() {
        UUID torchieId = whatsNextWheel.getLastWorker();
        expire(torchieId);

        whatsNextWheel.evictWorker(torchieId);

        circuitActivityDashboard.evictMonitor(MonitorType.TORCHIE_WORKER, torchieId);
    }



    @Override
    public int size() {
        return whatsNextWheel.size();
    }

    @Override
    public void reset() {
        evictAll();
    }

    private void evictAll() {
        Set<UUID> workerKeys = whatsNextWheel.getWorkerKeys();

        for (UUID workerId : workerKeys) {
            evictWorker(workerId);
        }
    }

    private void evictWorker(UUID workerId) {
        expire(workerId);

        whatsNextWheel.evictWorker(workerId);
        circuitActivityDashboard.evictMonitor(MonitorType.TORCHIE_WORKER, workerId);
    }

    @Override
    public TickInstructions whatsNext() {

        if (peekInstruction == null) {
            peek();
        }

        TickInstructions nextInstruction = peekInstruction;

        peekInstruction = null;

        return nextInstruction;
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

    public TorchieCmd submitJob(Torchie torchie) {
        whatsNextWheel.submit(torchie.getTorchieId(), torchie);

        return new TorchieCmd(whatsNextWheel, torchie);
    }

    public void clear() {
        whatsNextWheel.clear();
    }
}
