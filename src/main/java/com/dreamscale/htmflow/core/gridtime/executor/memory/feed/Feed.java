package com.dreamscale.htmflow.core.gridtime.executor.memory.feed;

import java.util.LinkedList;

public class Feed {

    private final int MAX_QUEUE_SIZE;
    private final int FETCH_SIZE;

    private final LinkedList<Flowable> inputQueue;

    public Feed() {
        inputQueue = new LinkedList<Flowable>();
        this.MAX_QUEUE_SIZE = 300;
        this.FETCH_SIZE = 100;
    }

    public Feed(int maxQueueSize, int fetchSize) {
        inputQueue = new LinkedList<Flowable>();
        this.MAX_QUEUE_SIZE = maxQueueSize;
        this.FETCH_SIZE = fetchSize;
    }

    void subscribe() {

    }

    public boolean iAmReadyForMore() {
        return  (MAX_QUEUE_SIZE - inputQueue.size() > FETCH_SIZE);
    }
}
