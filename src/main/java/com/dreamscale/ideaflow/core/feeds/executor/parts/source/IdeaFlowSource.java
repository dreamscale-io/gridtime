package com.dreamscale.ideaflow.core.feeds.executor.parts.source;


import com.dreamscale.ideaflow.core.feeds.common.*;
import com.dreamscale.ideaflow.core.feeds.executor.parts.fetch.Batch;
import com.dreamscale.ideaflow.core.feeds.common.SharedFeaturePool;
import com.dreamscale.ideaflow.core.feeds.executor.parts.fetch.FetchStrategy;
import com.dreamscale.ideaflow.core.feeds.story.see.IdeaFlowObserver;
import com.dreamscale.ideaflow.core.feeds.story.see.Window;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class IdeaFlowSource implements IdeaFlow {

    private final LinkedList<Flowable> inputQueue;

    private final FetchStrategy fetchStrategy;
    private final UUID memberId;
    private final List<IdeaFlowObserver> flowObservers;
    private final SharedFeaturePool sharedFeaturePool;

    private Bookmark currentBookmark;

    private static final int MAX_QUEUE_SIZE = 300;

    private static final int FETCH_SIZE = 100;


    public IdeaFlowSource(UUID memberId, SharedFeaturePool sharedFeaturePool, FetchStrategy fetchStrategy, IdeaFlowObserver... observers) {
        this.memberId = memberId;
        this.inputQueue = new LinkedList<Flowable>();
        this.sharedFeaturePool = sharedFeaturePool;

        this.fetchStrategy = fetchStrategy;

        this.flowObservers = new ArrayList<IdeaFlowObserver>();
        for (IdeaFlowObserver observer : observers) {
            addFlowObserver(observer);
        }
    }

    public void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) throws InterruptedException {
        initBookmarkIfNeeded(fromClockPosition);

        pullMoreIfCapacityAvailable();

        Window window = createNextWindow(fromClockPosition, toClockPosition);
        observeFlowables(window);

        while (!window.isFinished() && inputQueue.isEmpty()) {
            int recordsPulled = pullMoreIfCapacityAvailable();

            if (recordsPulled == 0) {
                break;
            } else {
                window = createNextWindow(fromClockPosition, toClockPosition);
                observeFlowables(window);
            }
        }
    }

    private void observeFlowables(Window window) {
        for (IdeaFlowObserver observer : flowObservers) {
            observer.see(sharedFeaturePool.getActiveStoryFrame(), window);
        }
    }


    @Override
    public void addFlowObserver(IdeaFlowObserver observer) {
        this.flowObservers.add(observer);
    }


    private void initBookmarkIfNeeded(LocalDateTime clockPosition) {
        if (this.currentBookmark == null) {
            this.currentBookmark = new Bookmark(clockPosition);
        }
    }

    public int pullMoreIfCapacityAvailable() {
        int recordsPulled = 0;

        if (iAmReadyForMore()) {
            Batch batch = fetchStrategy.fetchNextBatch(memberId, this.currentBookmark, FETCH_SIZE);
            inputQueue.addAll(batch.getFlowables());
            if (batch.size() > 0) {
                this.currentBookmark = batch.getNextBookmark();
                recordsPulled = batch.size();
            }
        }
        return recordsPulled;
    }

    private boolean iAmReadyForMore() {
        return  (MAX_QUEUE_SIZE - inputQueue.size() > FETCH_SIZE);
    }

    private Window createNextWindow(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) throws InterruptedException {

        Window window = new Window(fromClockPosition, toClockPosition);

        boolean wantMore = true;

        while (wantMore && !inputQueue.isEmpty()) {
            Flowable flowable = inputQueue.peek();

            if (window.hasTimeInWindow(flowable)) {
                flowable = inputQueue.removeFirst();

                if (window.hasTimeAfterWindow(flowable)) {
                    Flowable splitFlowable = window.splitTimeAfterWindowIntoNewFlowable(flowable);
                    inputQueue.push(splitFlowable);

                    window.addAndTrimToFit(flowable);
                } else {
                    window.add(flowable);
                }
            } else {
                wantMore = false;
            }

            if (window.hasTimeAfterWindow(flowable)) {
                window.setFinished(true);
            }

        }

        return window;
    }

}