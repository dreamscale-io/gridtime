package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source;


import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.Flow;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer.FlowObserver;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Feed;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlowSource<T extends Flowable> implements Flow {

    private final List<FlowObserver<T>> flowObservers;
    private final FeaturePool featurePool;
    private final Feed<T> sourceFeed;

    public FlowSource(UUID memberId, FeaturePool featurePool, FeedStrategy<T> feedStrategy, FlowObserver<T>... observers) {
        this.featurePool = featurePool;
        this.sourceFeed = featurePool.registerFeed(memberId, feedStrategy);

        this.flowObservers = new ArrayList<>();

        for (FlowObserver<T> observer : observers) {
            addFlowObserver(observer);
        }
    }

    public void tick(Metronome.Tick tick) {

        LocalDateTime fromClockPosition = tick.getFrom().getClockTime();
        LocalDateTime toClockPosition = tick.getTo().getClockTime();

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

            observer.see(window, featurePool);
        }
    }


    private void addFlowObserver(FlowObserver<T> observer) {
        this.flowObservers.add(observer);
    }



}
