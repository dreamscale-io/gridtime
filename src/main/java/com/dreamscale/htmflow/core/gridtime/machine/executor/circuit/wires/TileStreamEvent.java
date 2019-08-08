package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires;

import com.dreamscale.htmflow.core.domain.work.WorkToDoType;
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class TileStreamEvent {

    UUID teamId;
    UUID torchieId;
    GeometryClock.GridTime gridTime;
    WorkToDoType eventType;
}
