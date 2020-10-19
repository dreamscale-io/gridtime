package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity;
import com.dreamscale.gridtime.core.domain.flow.FlowActivityRepository;
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity;
import com.dreamscale.gridtime.core.domain.journal.IntentionRepository;
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity;
import com.dreamscale.gridtime.core.domain.member.TeamMemberRepository;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorEntity;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorRepository;
import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.TorchieFactory;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.NoOpInstruction;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.GridSyncLockManager;
import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard;
import com.dreamscale.gridtime.core.machine.executor.dashboard.MonitorType;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TorchieFactory torchieFactory;

    private LocalDateTime lastSyncCheck;
    private TickInstructions peekInstruction;

    private final WhatsNextWheel whatsNextWheel = new WhatsNextWheel();

    private final Duration syncInterval = Duration.ofMinutes(5);
    private final Duration expireWhenStaleMoreThan = Duration.ofMinutes(60);

    private static final int MAX_TORCHIES = 10;
    private boolean paused = false;

    private UUID serverClaimId = UUID.randomUUID();

    @Transactional
    public void sync() {
        LocalDateTime now = gridClock.now();
        if (lastSyncCheck == null || now.isAfter(lastSyncCheck.plus(syncInterval))) {
            lastSyncCheck = now;

            gridSyncLockManager.tryToAcquireTorchieSyncLock();

            initializeMissingTorchies(now);
            claimTorchiesReadyForProcessing(now);
            //expireZombieTorchies(now);

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

    private void claimTorchiesReadyForProcessing(LocalDateTime now) {
        log.debug ("claimTorchiesReadyForProcessing = torchies = " + whatsNextWheel.size());

        int claimUpTo = MAX_TORCHIES - whatsNextWheel.size();
        List<Torchie> torchies = claimUpTo(now, claimUpTo);

        for (Torchie torchie : torchies) {
            whatsNextWheel.addWorker(torchie.getTorchieId(), torchie);
            circuitActivityDashboard.addMonitor(MonitorType.TORCHIE_WORKER, torchie.getTorchieId(), torchie.getCircuitMonitor());
        }
    }

    protected void initializeMissingTorchies(LocalDateTime now) {
        List<TeamMemberEntity> missingTorchies = teamMemberRepository.selectMissingTorchies();
        List<TorchieFeedCursorEntity> readyTorchies = new ArrayList<>();

        for (TeamMemberEntity teamMember : missingTorchies) {

            LocalDateTime firstTilePosition = findFirstTilePosition(teamMember.getMemberId());
            LocalDateTime lastPublishedDataPosition = findLastPublishedData(teamMember.getMemberId());

            if (firstTilePosition != null && lastPublishedDataPosition != null) {

                log.debug ("FIRST TILE POSITION: "+firstTilePosition);
                log.debug ("LAST PUBLISHED POSITION: "+lastPublishedDataPosition);

                TorchieFeedCursorEntity torchieCursor = new TorchieFeedCursorEntity();
                torchieCursor.setId(UUID.randomUUID());
                torchieCursor.setTorchieId(teamMember.getMemberId());
                torchieCursor.setOrganizationId(teamMember.getOrganizationId());
                torchieCursor.setTeamId(teamMember.getTeamId());
                torchieCursor.setFirstTilePosition(firstTilePosition);
                torchieCursor.setLastPublishedDataCursor(lastPublishedDataPosition);
                torchieCursor.setNextWaitUntilCursor(firstTilePosition.plus(ZoomLevel.TWENTY.getDuration()));
                torchieCursor.setLastClaimUpdate(now);
                readyTorchies.add(torchieCursor);
            } else {
                log.warn("Skipping torchie feed: "+teamMember.getMemberId() + ", waiting for data");
            }
        }

        torchieFeedCursorRepository.save(readyTorchies);
    }

    @Transactional
    protected List<Torchie> claimUpTo(LocalDateTime now, int limit) {

        List<TorchieFeedCursorEntity> unclaimedTorchies = torchieFeedCursorRepository.findUnclaimedTorchies(limit);

        List<Torchie> claimedTorchies = new ArrayList<>();

        for (TorchieFeedCursorEntity cursor : unclaimedTorchies) {

            log.debug("Claiming "+cursor.getTorchieId() + " with "+serverClaimId);

            cursor.setClaimingServerId(serverClaimId);
            cursor.setLastClaimUpdate(now);

            TorchieFeedCursorEntity updated = torchieFeedCursorRepository.save(cursor);

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
        return torchieFactory.wireUpMemberTorchie(cursor.getTeamId(), cursor.getTorchieId(), getStartPosition(cursor), getRunUntil(cursor));
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

    private LocalDateTime getRunUntil(TorchieFeedCursorEntity cursor) {

        LocalDateTime runUntilPosition = null;

        if (cursor.getLastPublishedDataCursor() != null) {
            runUntilPosition = GeometryClock.roundDownToNearestTwenty(cursor.getLastPublishedDataCursor()).minus(ZoomLevel.TWENTY.getDuration());
        }

        return runUntilPosition;
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

    @Transactional
    protected void expire(UUID torchieId) {
       torchieFeedCursorRepository.expire(torchieId);
    }


    @Transactional
    public void expireZombieTorchies(LocalDateTime now) {

        LocalDateTime expireBeforeDate = now.minus(expireWhenStaleMoreThan);

        //expire torchies that dont seem to be running anymore so they can be reclaimed
        torchieFeedCursorRepository.expireZombieTorchies(Timestamp.valueOf(expireBeforeDate));
    }

    public boolean hasWork() {

        if (peekInstruction == null) {
            peek();
        }
        return (peekInstruction != null ) ;
    }

    private void evictLastWorker() {
        if (paused) return;

        UUID torchieId = whatsNextWheel.getLastWorker();

        if (torchieId != null) {
            expire(torchieId);

            whatsNextWheel.evictWorker(torchieId);
            circuitActivityDashboard.evictMonitor(torchieId);
        }
    }



    @Override
    public int size() {
        return whatsNextWheel.size();
    }

    @Override
    public void reset() {
        evictAll();
        peekInstruction = null;

        paused = false;
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
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
        circuitActivityDashboard.evictMonitor(workerId);
    }

    @Override
    public TickInstructions whatsNext() {
        if (paused) return null;

        peek();

        TickInstructions nextInstruction = peekInstruction;

        peekInstruction = null;

        return nextInstruction;
    }

    private void peek() {

        if (peekInstruction == null ) {
            peekInstruction = whatsNextWheel.whatsNext();

            while (peekInstruction == null && whatsNextWheel.isNotExhausted()) {
                evictLastWorker();
                peekInstruction = whatsNextWheel.whatsNext();
            }
        }
        if (peekInstruction instanceof NoOpInstruction) {
            peekInstruction = null;
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
