package com.dreamscale.gridtime.core.machine.executor.circuit.now;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.AlarmScript;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.TimeBomb;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.IdeaFlowMetrics;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;

import java.util.List;

public class TwilightMap {
    public void push(IdeaFlowMetrics ideaFlowTile) {
        //TODO build a signals model for monitoring

    }

    public List<TimeBomb> generateTimeBombMonitors() {
        return DefaultCollections.list();
    }

    public List<AlarmScript> triggerAlarms() {
        return DefaultCollections.list();
    }


    public Integer count(PlaceType p) {
        return 0;
    }

    public boolean contains(PlaceReference p) {
        return true;
    }

    public TwilightMap selectFeaturesAbovePainThreshold(int p) {
        return null;
    }

    public int size() {
        return 0;
    }
}
