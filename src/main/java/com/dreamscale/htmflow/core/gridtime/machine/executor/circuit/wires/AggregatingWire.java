package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires;

import com.dreamscale.htmflow.core.domain.work.WorkToDoType;
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
public class AggregatingWire implements Wire {

    private final LinkedHashMap<String, AggregateStreamEvent> aggregateEventStream;
    private final UUID teamId;

    public AggregatingWire(UUID teamId) {
        this.teamId = teamId;
        aggregateEventStream = DefaultCollections.map();
    }


    public void pushAll(List<TileStreamEvent> tileStreamEvents) {
        for (TileStreamEvent event : tileStreamEvents) {
            push(event);
        }
    }

    public void push(TileStreamEvent event) {
        synchronized (aggregateEventStream) {
            AggregateStreamEvent aggregate = findOrCreateAggregateByGridTime(event.gridTime, event.eventType);
            aggregate.add(event);
        }
    }

    public boolean hasNext() {
        return aggregateEventStream.size() > 0;
    }


    @Override
    public AggregateStreamEvent pullNext(UUID workerId) {
        AggregateStreamEvent aggregate;

        synchronized (aggregateEventStream) {
            String firstKey = aggregateEventStream.keySet().iterator().next();
            aggregate = aggregateEventStream.remove(firstKey);
        }

        return aggregate;
    }

    @Override
    public void markDone(UUID workerId) {

    }

    @Override
    public int getQueueDepth() {
        return aggregateEventStream.size();
    }

    private AggregateStreamEvent findOrCreateAggregateByGridTime(GeometryClock.GridTime gridTime, WorkToDoType eventType) {

        String aggregateKey = createAggregateKey(gridTime, eventType);
        AggregateStreamEvent aggregate = aggregateEventStream.get(aggregateKey);

        if (aggregate == null) {
            aggregate = new AggregateStreamEvent(teamId, gridTime, eventType);
            aggregateEventStream.put(aggregateKey, aggregate);
        }
        return aggregate;
    }

    private String createAggregateKey(GeometryClock.GridTime gridTime, WorkToDoType eventType) {
        return gridTime.toDisplayString() + "::" + eventType.name();
    }


}
