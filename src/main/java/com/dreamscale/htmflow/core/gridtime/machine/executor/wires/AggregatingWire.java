package com.dreamscale.htmflow.core.gridtime.machine.executor.wires;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;

@Slf4j
public class AggregatingWire {

    private final LinkedHashMap<String, AggregateStreamEvent> aggregateEventStream;

    public AggregatingWire() {
        aggregateEventStream = DefaultCollections.map();
    }

    public void sendTileStreamEvent(TileStreamEvent event) {

        synchronized (aggregateEventStream) {
            AggregateStreamEvent aggregate = findOrCreateAggregateByGridTime(event.gridTime, event.streamAction);
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

    private AggregateStreamEvent findOrCreateAggregateByGridTime(GeometryClock.GridTime gridTime, StreamAction streamAction) {

        String aggregateKey = createAggregateKey(gridTime, streamAction);
        AggregateStreamEvent aggregate = aggregateEventStream.get(aggregateKey);

        if (aggregate == null) {
            aggregate = new AggregateStreamEvent(gridTime, streamAction);
            aggregateEventStream.put(aggregateKey, aggregate);
        }
        return aggregate;
    }

    private String createAggregateKey(GeometryClock.GridTime gridTime, StreamAction streamAction) {
        return gridTime.toDisplayString() + "::" + streamAction.name();
    }




}
