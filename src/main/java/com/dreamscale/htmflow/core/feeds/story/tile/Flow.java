package com.dreamscale.htmflow.core.feeds.story.tile;

import java.util.concurrent.ArrayBlockingQueue;

public class Flow<T extends Tile> {

    private static final int TILE_BUFFER = 10;
    private final ZoomLevel zoomLevel;

    //can ultimately externalize this channel, make this work off of a logger feed, chat channel
    ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<T>(TILE_BUFFER);


    public Flow(ZoomLevel zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    T next() throws InterruptedException {
        return queue.take();
    }
}
