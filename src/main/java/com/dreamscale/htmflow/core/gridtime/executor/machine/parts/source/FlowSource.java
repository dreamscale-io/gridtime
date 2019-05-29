package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source;


import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.Flow;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.Batch;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.memory.PerProcessFeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer.FlowObserver;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Flowable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FlowSource implements Flow {

    private final LinkedList<Flowable> inputQueue;

    private final FetchStrategy fetchStrategy;
    private final UUID memberId;
    private final List<FlowObserver> flowObservers;
    private final FeaturePool featurePool;

    private Bookmark currentBookmark;

    private static final int MAX_QUEUE_SIZE = 300;

    private static final int FETCH_SIZE = 100;


    public FlowSource(UUID memberId, FeaturePool featurePool, FetchStrategy fetchStrategy, FlowObserver... observers) {
        this.memberId = memberId;
        this.inputQueue = new LinkedList<Flowable>();
        this.featurePool = featurePool;

        this.fetchStrategy = fetchStrategy;

        this.flowObservers = new ArrayList<FlowObserver>();
        for (FlowObserver observer : observers) {
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
        for (FlowObserver observer : flowObservers) {

            observer.seeInto(window.getFlowables(), featurePool.getActiveGridTile());
        }
    }


    private void addFlowObserver(FlowObserver observer) {
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