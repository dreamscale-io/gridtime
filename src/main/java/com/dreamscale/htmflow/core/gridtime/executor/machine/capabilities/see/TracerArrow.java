package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.see;

import java.util.concurrent.ArrayBlockingQueue;

public class TracerArrow<S> {

    private final TracerArrowCapabilities tracerArrowCapabilities;
    ArrayBlockingQueue<S> tracerOfPlacesWhereIveBeen;

    public TracerArrow(TracerArrowCapabilities tracerArrowCapabilities) {
        this.tracerArrowCapabilities = tracerArrowCapabilities;
    }

    public void push(S seeFromPlace) {

    }
}
