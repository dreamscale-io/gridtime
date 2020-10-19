package com.dreamscale.gridtime.core.machine.memory.feed;

import com.dreamscale.gridtime.api.grid.Observable;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.Batch;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Bookmark;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.UUID;

public class InputFeed<T extends Flowable> implements Observable {

    private final int MAX_QUEUE_SIZE = 300;
    private final int FETCH_SIZE = 100;

    private final LinkedList<T> inputQueue;
    private final UUID memberId;

    private final FeedStrategyFactory.FeedType feedType;
    private final FeedStrategy<T> feedStrategy;

    private Bookmark fetchLocationBookmark;

    public InputFeed(FeedStrategyFactory.FeedType feedType, UUID memberId, FeedStrategy<T> feedStrategy) {
        this.feedType = feedType;
        this.inputQueue = DefaultCollections.queueList();
        this.memberId = memberId;
        this.feedStrategy = feedStrategy;
    }

    public int pullMoreIfCapacityAvailable(LocalDateTime currentWindowPosition) {
        initBookmarkIfNeeded(currentWindowPosition);

        int recordsPulled = 0;

        if (iAmReadyForMore()) {
            Batch<T> batch = feedStrategy.fetchNextBatch(memberId, this.fetchLocationBookmark, FETCH_SIZE);
            inputQueue.addAll(batch.getFlowables());
            if (batch.size() > 0) {
                this.fetchLocationBookmark = batch.getNextBookmark();
                recordsPulled = batch.size();
            }
        }
        return recordsPulled;
    }

    public void addSomeData(T flowable) {
        inputQueue.add(flowable);
    }

    private void initBookmarkIfNeeded(LocalDateTime clockPosition) {
        if (this.fetchLocationBookmark == null) {
            this.fetchLocationBookmark = new Bookmark(clockPosition);
        }
    }

    public Window<T> pullNextWindow(LocalDateTime fromClockPosition, LocalDateTime toClockPosition)  {

        Window<T> window = new Window<>(fromClockPosition, toClockPosition);

        boolean wantMore = true;

        while (wantMore && !inputQueue.isEmpty()) {
            T flowable = inputQueue.peek();

            if (window.hasTimeInWindow(flowable)) {
                flowable = inputQueue.removeFirst();

                if (window.hasTimeAfterWindow(flowable)) {
                    T splitFlowable = window.splitTimeAfterWindowIntoNewFlowable(flowable);
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

    public boolean iAmReadyForMore() {
        return  (MAX_QUEUE_SIZE - inputQueue.size() > FETCH_SIZE);
    }

    public boolean isEmpty() {
        return inputQueue.isEmpty();
    }

    @Override
    public String toDisplayString() {
        return feedType.name() + "["+inputQueue.size()+"]";
    }


}
