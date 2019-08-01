package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
public class DevNullWire implements Wire {


    @Override
    public void publishAll(List<TileStreamEvent> tileStreamEvents) {

    }

    @Override
    public void publish(TileStreamEvent event) {

    }
}
