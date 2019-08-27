package com.dreamscale.gridtime.core.machine.executor.circuit.wires;

import com.dreamscale.gridtime.core.domain.work.WorkToDoType;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class AggregateStreamEvent {

    private final UUID teamId;
    private final GeometryClock.GridTime gridTime;
    private final WorkToDoType eventType;
    private List<TileStreamEvent> sourceTileEvents;

    public AggregateStreamEvent(UUID teamId, GeometryClock.GridTime gridTime, WorkToDoType eventType) {
        this.teamId = teamId;
        this.gridTime = gridTime;
        this.eventType = eventType;
        this.sourceTileEvents = new ArrayList<>();
    }

    public ZoomLevel getZoomLevel() {
        return gridTime.getZoomLevel();
    }

    public void add(TileStreamEvent tileStreamEvent) {
        sourceTileEvents.add(tileStreamEvent);
    }
}
