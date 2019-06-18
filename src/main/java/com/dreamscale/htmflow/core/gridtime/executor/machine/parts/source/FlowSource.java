package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source;


import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.Flow;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer.FlowObserver;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Feed;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Flowable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlowSource<T extends Flowable> implements Flow {

    private final List<FlowObserver<T>> flowObservers;
    private final FeaturePool featurePool;
    private final Feed<T> sourceFeed;

    public FlowSource(UUID memberId, FeaturePool featurePool, FetchStrategy<T> fetchStrategy, FlowObserver<T>... observers) {
        this.featurePool = featurePool;
        this.sourceFeed = featurePool.registerFeed(memberId, fetchStrategy);

        this.flowObservers = new ArrayList<>();

        for (FlowObserver<T> observer : observers) {
            addFlowObserver(observer);
        }
    }

    public void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) throws InterruptedException {

        sourceFeed.pullMoreIfCapacityAvailable(fromClockPosition);

        Window<T> window = sourceFeed.pullNextWindow(fromClockPosition, toClockPosition);
        observeFlowables(window);

        while (!window.isFinished() && sourceFeed.isEmpty()) {
            int recordsPulled = sourceFeed.pullMoreIfCapacityAvailable(fromClockPosition);

            if (recordsPulled == 0) {
                break;
            } else {
                window = sourceFeed.pullNextWindow(fromClockPosition, toClockPosition);
                observeFlowables(window);
            }
        }
    }

    private void observeFlowables(Window<T> window) {
        for (FlowObserver<T> observer : flowObservers) {

            observer.see(window, featurePool.getActiveGridTile());
        }
    }


    private void addFlowObserver(FlowObserver<T> observer) {
        this.flowObservers.add(observer);
    }



}
