package com.dreamscale.htmflow.core.gridtime.machine.executor.wires;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;

import java.util.UUID;

public class TileStreamEvent {

    UUID tileOwnerId;
    GeometryClock.GridTime gridTime;
    StreamAction streamAction;
}
