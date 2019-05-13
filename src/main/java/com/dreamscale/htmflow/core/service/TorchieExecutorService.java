package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.torchie.TorchieJobStatus;
import com.dreamscale.htmflow.core.domain.tile.TorchieBookmarkEntity;
import com.dreamscale.htmflow.core.domain.tile.TorchieBookmarkRepository;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.executor.Torchie;
import com.dreamscale.htmflow.core.feeds.executor.TorchieFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component
public class TorchieExecutorService {

    @Autowired
    private TorchieFactory torchieFactory;

    @Autowired
    private TorchieBookmarkRepository torchieBookmarkRepository;

    @Autowired
    private TimeService timeService;

    @Autowired
    private AccountService accountService;

    private ThreadPoolExecutor executorPool;

    private static final int POOL_SIZE = 10;
    private static final int LOOK_FOR_MORE_WORK_DELAY = 100;

    private Map<UUID, Torchie> activeTorchiePool;
    private boolean isJobRunning;

    @PostConstruct
    public void init() {
        this.executorPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(POOL_SIZE);
        this.activeTorchiePool = new LinkedHashMap<>();

    }

    public TorchieJobStatus startMemberTorchie(UUID memberId) {

        TorchieJobStatus status = null;

        if (!activeTorchiePool.containsKey(memberId)) {
            LocalDateTime startingPosition = determineStartingPositionForMemberFeed(memberId);

            Torchie torchie = torchieFactory.wireUpMemberTorchie(memberId, startingPosition);
            addTorchieToJobPool(torchie);

            status = torchie.getJobStatus();
        } else {
            status = activeTorchiePool.get(memberId).getJobStatus();
        }

        return status;
    }

    public TorchieJobStatus startTeamTorchie(UUID teamId) {

        LocalDateTime startingPosition = determineStartingPositionForTeamFeed(teamId);

        Torchie torchie = torchieFactory.wireUpTeamTorchie(teamId, startingPosition);
        addTorchieToJobPool(torchie);

        return torchie.getJobStatus();
    }


    public void runAllTorchies() throws InterruptedException {
        isJobRunning = true;

        while (isJobRunning) {
            int workToDoThisIteration = 0;

            //run a wave of filling the job queue, only if there's not existing blocked jobs
            if (executorPool.getQueue().size() == 0) {

                for (Torchie torchie : getActiveTorchies()) {

                    Runnable runnableJob = torchie.whatsNext();

                    if (runnableJob != null) {
                        workToDoThisIteration++;
                        log.info("Submitting job: " + torchie.getTorchieId());
                        executorPool.submit(runnableJob);
                    }

                    if (torchie.isDone()) {
                        removeTorchieFromJobPool(torchie);
                    }
                }
            }

            if (workToDoThisIteration == 0) {
                log.info("Sleeping..." + executorPool.getActiveCount());
                Thread.sleep(LOOK_FOR_MORE_WORK_DELAY);
            }

            if (activeTorchiePool.size() == 0) {
                isJobRunning = false;
            }

        }
    }

    private List<Torchie> getActiveTorchies() {
        return new ArrayList<>(activeTorchiePool.values());
    }


    public void stopAllTorchies() {
        this.isJobRunning = false;

        for (Torchie torchie : activeTorchiePool.values()) {
            torchie.wrapUpAndBookmark();
            activeTorchiePool.remove(torchie.getTorchieId());
        }
    }

    public void stopTorchie(UUID memberId) {
        for (Torchie torchie : activeTorchiePool.values()) {
            if (torchie.getTorchieId() == memberId) {
                torchie.wrapUpAndBookmark();
                activeTorchiePool.remove(torchie.getTorchieId());
                break;
            }
        }
    }

    private void addTorchieToJobPool(Torchie torchie) {
        this.activeTorchiePool.put(torchie.getTorchieId(), torchie);
    }

    private void removeTorchieFromJobPool(Torchie torchie) {
        this.activeTorchiePool.remove(torchie.getTorchieId());
    }


    private LocalDateTime determineStartingPositionForMemberFeed(UUID memberId) {
        LocalDateTime startingPosition = null;

        TorchieBookmarkEntity torchieBookmark = torchieBookmarkRepository.findByTorchieId(memberId);
        if (torchieBookmark != null) {
           startingPosition = torchieBookmark.getMetronomeCursor();
        } else {
            LocalDateTime activationDate = accountService.getActivationDateForMember(memberId);
            startingPosition = GeometryClock.roundDownToNearestTwenty(activationDate);
        }
        return startingPosition;
    }


    private LocalDateTime determineStartingPositionForTeamFeed(UUID teamId) {
        //TODO this should first check saved processing output to determine starting bookmark
        //then otherwise, get earliest of team member starts
        //but default to activation date
        return null;
    }

    public List<TorchieJobStatus> getAllJobStatus() {
        List<TorchieJobStatus> jobStatuses = new ArrayList<>();

        for (Torchie torchie : activeTorchiePool.values()) {
            jobStatuses.add(torchie.getJobStatus());
        }
        return jobStatuses;
    }
}
