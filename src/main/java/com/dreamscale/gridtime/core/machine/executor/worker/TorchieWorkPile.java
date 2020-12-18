package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.api.grid.Results;
import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity;
import com.dreamscale.gridtime.core.domain.flow.FlowActivityRepository;
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity;
import com.dreamscale.gridtime.core.domain.journal.IntentionRepository;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorEntity;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorRepository;
import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.TorchieFactory;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyDoneTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyFailureTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.NoOpInstruction;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.lock.GridSyncLockManager;
import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard;
import com.dreamscale.gridtime.core.machine.executor.dashboard.MonitorType;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class TorchieWorkPile implements WorkPile, LiveQueue {

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
    private TeamRepository teamRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private TorchieFactory torchieFactory;

    private LocalDateTime lastSyncCheck;
    private TickInstructions peekInstruction;

    private final WhatsNextWheel whatsNextWheel = new WhatsNextWheel();

    private final Duration syncInterval = Duration.ofMinutes(5);
    private final Duration expireWhenStaleMoreThan = Duration.ofMinutes(10);

    private static final int MAX_TORCHIES = 10;
    private boolean paused = false;
    private boolean autorun = true;

    private UUID serverClaimId = UUID.randomUUID();

    @Transactional
    public void sync() {
        LocalDateTime now = gridClock.now();
        if (lastSyncCheck == null || now.isAfter(lastSyncCheck.plus(syncInterval))) {
            lastSyncCheck = now;

            gridSyncLockManager.tryToAcquireTorchieSyncLock();

            try {
                initializeMissingTorchies(now);
                if (autorun) {
                    claimTorchiesReadyForProcessing(now);
                }
                updateKeepAliveOnClaimedTorchies(now);
                purgeEvictedTorchies(now);
                expireZombieTorchies(now);
            } finally {
                gridSyncLockManager.releaseTorchieSyncLock();
            }
        }
    }

    private void purgeEvictedTorchies(LocalDateTime now) {

        List<UUID> purged = whatsNextWheel.purgeEvicted(now);

        for (UUID workerId : purged) {
            circuitActivityDashboard.evictMonitor(workerId);
            expireClaim(workerId);
        }

    }

    private void updateKeepAliveOnClaimedTorchies(LocalDateTime now) {
        Set<UUID> torchieIds = whatsNextWheel.getWorkerKeys();

        List<TorchieFeedCursorEntity> cursorUpdates = new ArrayList<>();

        for (UUID torchieId : torchieIds) {
            TorchieFeedCursorEntity cursor = torchieFeedCursorRepository.findByTorchieId(torchieId);
            cursor.setLastClaimUpdate(now);
            cursorUpdates.add(cursor);
        }
        torchieFeedCursorRepository.save(cursorUpdates);
    }

    public TorchieCmd getTorchieCmd(UUID torchieId) {

        Torchie torchie = (Torchie) whatsNextWheel.getWorker(torchieId);

        if (torchie == null) {
            torchie = loadTorchie(torchieId);
        }

        return new TorchieCmd(this, torchie);
    }

    @Override
    public void submitToLiveQueue(Torchie torchie) {

        UUID torchieId = torchie.getTorchieId();

        LocalDateTime now = gridClock.now();

        if (whatsNextWheel.contains(torchieId)) {
            whatsNextWheel.unmarkForEviction(torchieId);
        } else {
            try {
                gridSyncLockManager.tryToAcquireTorchieSyncLock();

                TorchieFeedCursorEntity cursor = torchieFeedCursorRepository.findByTorchieId(torchieId);
                if (cursor != null) {
                    claimTorchie(now, cursor);
                }
                circuitActivityDashboard.addMonitor(MonitorType.TORCHIE_WORKER, torchieId, torchie.getCircuitMonitor());
                whatsNextWheel.addWorker(torchieId, torchie);
            } catch (Exception ex) {
                log.error("Unable to claim torchie", ex);
            } finally {
                gridSyncLockManager.releaseTorchieSyncLock();
            }

        }
    }

    private void claimTorchiesReadyForProcessing(LocalDateTime now) {
        log.debug ("claimTorchiesReadyForProcessing = torchies = " + whatsNextWheel.size());

        int claimUpTo = MAX_TORCHIES - whatsNextWheel.size();
        List<Torchie> torchies = claimUpTo(now, claimUpTo);

        for (Torchie torchie : torchies) {
            NotifyTorchieDoneTrigger trigger = new NotifyTorchieDoneTrigger(torchie.getTorchieId());
            torchie.notifyOnDone(trigger);
            torchie.notifyOnFail(trigger);
            circuitActivityDashboard.addMonitor(MonitorType.TORCHIE_WORKER, torchie.getTorchieId(), torchie.getCircuitMonitor());

            whatsNextWheel.addWorker(torchie.getTorchieId(), torchie);
        }
    }

    protected void initializeMissingTorchies(LocalDateTime now) {
        List<OrganizationMemberEntity> missingTorchies = organizationMemberRepository.selectMissingTorchies();
        List<TorchieFeedCursorEntity> readyTorchies = new ArrayList<>();

        for (OrganizationMemberEntity orgMember : missingTorchies) {

            LocalDateTime firstTilePosition = findFirstTilePosition(orgMember.getId());
            LocalDateTime lastPublishedDataPosition = findLastPublishedData(orgMember.getId());

            if (firstTilePosition != null && lastPublishedDataPosition != null) {

                log.debug ("FIRST TILE POSITION: "+firstTilePosition);
                log.debug ("LAST PUBLISHED POSITION: "+lastPublishedDataPosition);

                TorchieFeedCursorEntity torchieCursor = new TorchieFeedCursorEntity();
                torchieCursor.setId(UUID.randomUUID());
                torchieCursor.setTorchieId(orgMember.getId());
                torchieCursor.setOrganizationId(orgMember.getOrganizationId());
                torchieCursor.setFirstTilePosition(firstTilePosition);
                torchieCursor.setLastPublishedDataCursor(lastPublishedDataPosition);
                torchieCursor.setNextWaitUntilCursor(firstTilePosition.plus(ZoomLevel.TWENTY.getDuration()));
                torchieCursor.setLastClaimUpdate(now);
                readyTorchies.add(torchieCursor);
            } else {
                log.warn("Skipping torchie feed: "+orgMember.getId() + ", waiting for data");
            }
        }

        torchieFeedCursorRepository.save(readyTorchies);
    }

    @Transactional
    protected List<Torchie> claimUpTo(LocalDateTime now, int limit) {

        List<TorchieFeedCursorEntity> unclaimedTorchies = torchieFeedCursorRepository.findUnclaimedTorchies(limit);

        List<Torchie> claimedTorchies = new ArrayList<>();

        for (TorchieFeedCursorEntity cursor : unclaimedTorchies) {

            claimTorchie(now, cursor);

            Torchie torchie = loadTorchie(cursor);

            claimedTorchies.add(torchie);
        }

        return claimedTorchies;
    }

    private void claimTorchie(LocalDateTime now, TorchieFeedCursorEntity cursor) {
        log.debug("Claiming "+cursor.getTorchieId() + " with "+serverClaimId);

        cursor.setClaimingServerId(serverClaimId);
        cursor.setLastClaimUpdate(now);

        TorchieFeedCursorEntity updated = torchieFeedCursorRepository.save(cursor);
    }

    private Torchie loadTorchie(UUID torchieId) {
        TorchieFeedCursorEntity cursor = torchieFeedCursorRepository.findOne(torchieId);

        return loadTorchie(cursor);
    }

    private Torchie loadTorchie(TorchieFeedCursorEntity cursor) {
        List<UUID> teamIds = loadTeamIds(cursor.getOrganizationId(), cursor.getTorchieId());
        return torchieFactory.wireUpMemberTorchie(cursor.getOrganizationId(), cursor.getTorchieId(), teamIds, getStartPosition(cursor), getRunUntil(cursor));
    }

    private List<UUID> loadTeamIds(UUID organizationId, UUID memberId) {
        List<TeamEntity> teams = teamRepository.findMyTeamsByOrgMembership(organizationId, memberId);

        List<UUID> teamIds = new ArrayList<>();
        for (TeamEntity team : teams) {
            TeamType type = team.getTeamType();
            if (!type.equals(TeamType.ME) && !type.equals(TeamType.EVERYONE) ) {
                teamIds.add(team.getId());
            }
        }

        return teamIds;
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
    protected void expireClaim(UUID torchieId) {
       torchieFeedCursorRepository.expireClaim(torchieId);
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

    @Override
    public void shutdown() {
        evictAll();
        peekInstruction = null;
        lastSyncCheck = null;
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

    @Override
    public void setAutorun(boolean isAutorun) {
        this.autorun = isAutorun;
    }


    private void evictAll() {
        Set<UUID> workerKeys = whatsNextWheel.getWorkerKeys();

        for (UUID workerId : workerKeys) {
            evictWorker(workerId);
        }
    }

    private void evictWorker(UUID workerId) {
        whatsNextWheel.evictWorker(workerId);
        circuitActivityDashboard.evictMonitor(workerId);

        expireClaim(workerId);
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

            LocalDateTime now = gridClock.now();

            while (peekInstruction == null && whatsNextWheel.isNotExhausted()) {

                whatsNextWheel.markLastForEviction(now);

                peekInstruction = whatsNextWheel.whatsNext();
            }
        }
        if (peekInstruction instanceof NoOpInstruction) {
            peekInstruction = null;
        }
    }



    public void clear() {
        whatsNextWheel.clear();
    }



    private class NotifyTorchieDoneTrigger implements NotifyDoneTrigger, NotifyFailureTrigger {

        private final UUID torchieId;

        NotifyTorchieDoneTrigger(UUID torchieId) {
            this.torchieId = torchieId;
        }

        @Override
        @Transactional
        public void notifyWhenDone(TickInstructions instructions, List<Results> results) {
            evictWorker(torchieId);
        }

        @Override
        @Transactional
        public void notifyOnAbortOrFailure(TickInstructions instructions, Exception ex) {
            evictWorker(torchieId);

            TorchieFeedCursorEntity cursor = torchieFeedCursorRepository.findByTorchieId(torchieId);

            Integer fails = cursor.getFailureCount();
            if (fails == null) {
                fails = 1;
            } else {
                fails++;
            }

            cursor.setFailureCount(fails);
            cursor.setClaimingServerId(null);
            torchieFeedCursorRepository.save(cursor);


        }
    }
}
