package com.dreamscale.htmflow.core.gridtime.machine.executor.wires;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AggregateStreamEvent {

    private GeometryClock.GridTime gridTime;
    private StreamAction streamAction;
    private List<TileStreamEvent> sourceTileEvents;

    public AggregateStreamEvent(GeometryClock.GridTime gridTime, StreamAction streamAction) {
        this.gridTime = gridTime;
        this.streamAction = streamAction;
        this.sourceTileEvents = new ArrayList<>();
    }

    public void add(TileStreamEvent tileStreamEvent) {
        sourceTileEvents.add(tileStreamEvent);
    }
}
