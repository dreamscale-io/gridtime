package com.dreamscale.ideaflow.core.feeds.executor;

import com.dreamscale.ideaflow.core.feeds.common.ZoomableIdeaFlow;
import com.dreamscale.ideaflow.core.feeds.clock.Metronome;
import com.dreamscale.ideaflow.core.feeds.common.SharedFeaturePool;
import com.dreamscale.ideaflow.core.feeds.executor.parts.fetch.FetchStrategyFactory;
import com.dreamscale.ideaflow.core.feeds.story.see.IdeaFlowObserverFactory;
import com.dreamscale.ideaflow.core.feeds.executor.parts.source.IdeaFlowSource;
import com.dreamscale.ideaflow.core.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class TorchieExecutor {


    @Autowired
    private FetchStrategyFactory fetchStrategyFactory;

    @Autowired
    private IdeaFlowObserverFactory flowObserverFactory;

    @Autowired
    private AccountService accountService;

    private ThreadPoolExecutor executorPool;

    private static final int POOL_SIZE = 10;
    private static final int LOOK_FOR_MORE_WORK_DELAY = 1000;

    private ArrayList<Torchie> activeTorchiePool;
    private boolean isJobRunning;

    @PostConstruct
    public void init() {
        this.executorPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(POOL_SIZE);
        this.activeTorchiePool = new ArrayList<Torchie>();

    }

    public void startTorchie(UUID memberId) {

        LocalDateTime startingPosition = determineStartingPositionForFeed(memberId);
        Metronome metronome = new Metronome(startingPosition);

        Torchie torchie = wireFlowChainIntoTorchie(metronome, memberId);

        addTorchieToJobPool(torchie);
    }


    private Torchie wireFlowChainIntoTorchie(Metronome metronome, UUID memberId ) {

        SharedFeaturePool sharedFeaturePool = new SharedFeaturePool(memberId, metronome.getActiveCoordinates());

        metronome.addFlowToChain(new IdeaFlowSource(memberId, sharedFeaturePool,
                fetchStrategyFactory.get(FetchStrategyFactory.StrategyType.JOURNAL_FEED),
                flowObserverFactory.get(IdeaFlowObserverFactory.ObserverType.JOURNAL_CONTEXT_OBSERVER)
        ));

        metronome.addFlowToChain(new IdeaFlowSource(memberId, sharedFeaturePool,
                fetchStrategyFactory.get(FetchStrategyFactory.StrategyType.FILE_ACTIVITY_FEED),
                flowObserverFactory.get(IdeaFlowObserverFactory.ObserverType.COMPONENT_SPACE_OBSERVER)));

        ZoomableIdeaFlow zoomableIdeaFlow = new ZoomableIdeaFlow(metronome, memberId, sharedFeaturePool);

        return new Torchie(memberId, zoomableIdeaFlow);

    }

    public void runAllTorchies() throws InterruptedException {
        isJobRunning = true;

        while (isJobRunning) {
            for (Torchie torchie : activeTorchiePool) {

                if (executorPool.getQueue().size() == 0) {
                    Runnable runnableJob = torchie.whatsNext();

                    if (runnableJob != null) {
                        executorPool.submit(runnableJob);
                    }
                }
            }
            Thread.sleep(LOOK_FOR_MORE_WORK_DELAY);
        }
    }

    public void stopAllTorchies() {
        this.isJobRunning = false;

        for (Torchie torchie : activeTorchiePool) {
            torchie.wrapUpAndBookmark();
            activeTorchiePool.remove(torchie);
        }
    }

    public void stopTorchie(UUID memberId) {
        for (Torchie torchie : activeTorchiePool) {
            if (torchie.getMemberId() == memberId) {
                torchie.wrapUpAndBookmark();
                activeTorchiePool.remove(torchie);
                break;
            }
        }
    }

    private void addTorchieToJobPool(Torchie torchie) {
        this.activeTorchiePool.add(torchie);
    }

    private LocalDateTime determineStartingPositionForFeed(UUID memberId) {
        //TODO this should first check saved processing output to determine starting bookmark
        //but default to activation date
        return accountService.getActivationDateForMember(memberId);
    }

}
