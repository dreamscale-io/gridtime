package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
public class AggregatingWire implements Wire {

    private final LinkedHashMap<String, AggregateStreamEvent> aggregateEventStream;

    public AggregatingWire() {
        aggregateEventStream = DefaultCollections.map();
    }


    public void publishAll(List<TileStreamEvent> tileStreamEvents) {
        for (TileStreamEvent event : tileStreamEvents) {
            publish(event);
        }
    }

    public void publish(TileStreamEvent event) {
        synchronized (aggregateEventStream) {
            AggregateStreamEvent aggregate = findOrCreateAggregateByGridTime(event.gridTime, event.eventType);
            aggregate.add(event);
        }
    }

    public boolean hasNext() {
        return aggregateEventStream.size() > 0;
    }

    public AggregateStreamEvent pullNext() {
        AggregateStreamEvent aggregate;

        synchronized (aggregateEventStream) {
            String firstKey = aggregateEventStream.keySet().iterator().next();
            aggregate = aggregateEventStream.remove(firstKey);
        }

        return aggregate;
    }

    private AggregateStreamEvent findOrCreateAggregateByGridTime(GeometryClock.GridTime gridTime, EventType eventType) {

        String aggregateKey = createAggregateKey(gridTime, eventType);
        AggregateStreamEvent aggregate = aggregateEventStream.get(aggregateKey);

        if (aggregate == null) {
            aggregate = new AggregateStreamEvent(gridTime, eventType);
            aggregateEventStream.put(aggregateKey, aggregate);
        }
        return aggregate;
    }

    private String createAggregateKey(GeometryClock.GridTime gridTime, EventType eventType) {
        return gridTime.toDisplayString() + "::" + eventType.name();
    }


}
