package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AggregateStreamEvent {

    private GeometryClock.GridTime gridTime;
    private EventType eventType;
    private List<TileStreamEvent> sourceTileEvents;

    public AggregateStreamEvent(GeometryClock.GridTime gridTime, EventType eventType) {
        this.gridTime = gridTime;
        this.eventType = eventType;
        this.sourceTileEvents = new ArrayList<>();
    }

    public void add(TileStreamEvent tileStreamEvent) {
        sourceTileEvents.add(tileStreamEvent);
    }
}
