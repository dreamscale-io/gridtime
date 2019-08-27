package com.dreamscale.gridtime.core.machine.executor.circuit.wires;

import com.dreamscale.gridtime.core.domain.work.WorkToDoType;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
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
