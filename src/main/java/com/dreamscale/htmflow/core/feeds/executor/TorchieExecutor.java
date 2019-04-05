package com.dreamscale.htmflow.core.feeds.executor;

import com.dreamscale.htmflow.core.feeds.common.ZoomableFlow;
import com.dreamscale.htmflow.core.feeds.clock.Metronome;
import com.dreamscale.htmflow.core.feeds.common.SharedFeaturePool;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.FetchStrategyFactory;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.FlowSource;
import com.dreamscale.htmflow.core.feeds.executor.parts.observer.FlowObserverFactory;
import com.dreamscale.htmflow.core.service.AccountService;
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
    private FlowObserverFactory flowObserverFactory;

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

        metronome.addFlowToChain(new FlowSource(memberId, sharedFeaturePool,
                fetchStrategyFactory.get(FetchStrategyFactory.StrategyType.JOURNAL_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_CONTEXT_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_FEELS_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_AUTHOR_OBSERVER)
        ));

        metronome.addFlowToChain(new FlowSource(memberId, sharedFeaturePool,
                fetchStrategyFactory.get(FetchStrategyFactory.StrategyType.FILE_ACTIVITY_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.COMPONENT_SPACE_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.LEARNING_STATE_OBSERVER)));

        metronome.addFlowToChain(new FlowSource(memberId, sharedFeaturePool,
                fetchStrategyFactory.get(FetchStrategyFactory.StrategyType.EXECUTION_ACTIVITY_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.EXECUTION_RHYTHM_OBSERVER)));

        metronome.addFlowToChain(new FlowSource(memberId, sharedFeaturePool,
                fetchStrategyFactory.get(FetchStrategyFactory.StrategyType.CIRCLE_MESSAGES_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.WTF_STATE_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.CIRCLE_MESSAGE_OBSERVER)));


        ZoomableFlow zoomableFlow = new ZoomableFlow(metronome, memberId, sharedFeaturePool);

        return new Torchie(memberId, zoomableFlow);

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
