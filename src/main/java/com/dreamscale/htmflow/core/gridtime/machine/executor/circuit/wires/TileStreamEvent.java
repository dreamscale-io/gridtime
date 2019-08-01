package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class TileStreamEvent {

    UUID torchieId;
    GeometryClock.GridTime gridTime;
    EventType eventType;
}
