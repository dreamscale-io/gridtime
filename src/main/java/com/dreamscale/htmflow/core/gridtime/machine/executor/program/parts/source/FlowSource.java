package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source;


import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.Flow;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer.FlowObserver;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.InputFeed;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlowSource<T extends Flowable> implements Flow {

    private final List<FlowObserver<T>> flowObservers;
    private final TorchieState torchieState;
    private final InputFeed<T> inputFeed;

    public FlowSource(UUID memberId, TorchieState torchieState, FeedStrategy<T> feedStrategy, FlowObserver<T>... observers) {
        this.torchieState = torchieState;
        this.inputFeed = torchieState.registerInputFeed(memberId, feedStrategy.getFeedType(), feedStrategy);

        this.flowObservers = new ArrayList<>();

        for (FlowObserver<T> observer : observers) {
            addFlowObserver(observer);
        }
    }

    public void tick(Metronome.Tick tick) {

        LocalDateTime fromClockPosition = tick.getFrom().getClockTime();
        LocalDateTime toClockPosition = tick.getTo().getClockTime();

        inputFeed.pullMoreIfCapacityAvailable(fromClockPosition);

        Window<T> window = inputFeed.pullNextWindow(fromClockPosition, toClockPosition);
        observeFlowables(window);

        while (!window.isFinished() && inputFeed.isEmpty()) {
            int recordsPulled = inputFeed.pullMoreIfCapacityAvailable(fromClockPosition);

            if (recordsPulled == 0) {
                break;
            } else {
                window = inputFeed.pullNextWindow(fromClockPosition, toClockPosition);
                observeFlowables(window);
            }
        }
    }

    private void observeFlowables(Window<T> window) {
        for (FlowObserver<T> observer : flowObservers) {

            observer.see(window, torchieState);
        }
    }


    private void addFlowObserver(FlowObserver<T> observer) {
        this.flowObservers.add(observer);
    }



}
