package com.dreamscale.gridtime.core.machine.executor.circuit.now;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.AlarmScript;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.TimeBomb;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.IdeaFlowMetrics;

import java.util.List;

public class FitnessMatrix {
    public void push(IdeaFlowMetrics ideaFlowTile) {
        //TODO build a signals model for monitoring

    }

    public List<TimeBomb> generateTimeBombMonitors() {
        return DefaultCollections.list();
    }

    public List<AlarmScript> triggerAlarms() {
        return DefaultCollections.list();
    }



}
