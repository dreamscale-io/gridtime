package com.dreamscale.gridtime.core.machine.executor.circuit.now;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Scope {

    PlaceType placeTypeScope;
    ZoomLevel timeScope;
}
