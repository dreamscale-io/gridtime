package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.now;

import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.alarm.AlarmScript;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.alarm.TimeBomb;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.IdeaFlowMetrics;

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
