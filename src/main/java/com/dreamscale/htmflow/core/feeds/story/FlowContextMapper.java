package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextEndingEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.Movement;
import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowContextMapper {


    private Map<StructureLevel, ContextBeginningEvent> currentContextMap = new HashMap<>();
    private Map<StructureLevel, RelativeSequence> currentSequenceNumbers = new HashMap<>();

    private final LocalDateTime from;
    private final LocalDateTime to;

    private ContextEndingEvent contextToEndLater = null;

    public FlowContextMapper(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public ContextBeginningEvent getCurrentContext(StructureLevel structureLevel) {
        return currentContextMap.get(structureLevel);
    }

    public Movement beginContext(ContextBeginningEvent beginningEvent) {
        Movement movement = null;

        ContextBeginningEvent currentContext = currentContextMap.get(beginningEvent.getStructureLevel());

        if (currentContext == null) {
            this.currentContextMap.put(beginningEvent.getStructureLevel(), beginningEvent);
            movement = new Movement(beginningEvent.getPosition(), beginningEvent);
        } else {
            StructureLevel structureLevel = beginningEvent.getStructureLevel();
            RelativeSequence sequence = findOrCreateSequence(structureLevel);
            int nextSequence = sequence.increment();
            beginningEvent.setRelativeSequence(nextSequence);

            movement = new Movement(beginningEvent.getPosition(), beginningEvent);

            currentContextMap.put(structureLevel, beginningEvent);
        }
        return movement;
    }

    public Movement endContext(ContextEndingEvent endingEvent) {
        Movement movement = null;

        StructureLevel structureLevel = endingEvent.getStructureLevel();
        RelativeSequence sequence = findOrCreateSequence(structureLevel);
        int nextSequence = sequence.increment();
        endingEvent.setRelativeSequence(nextSequence);

        movement = new Movement(endingEvent.getPosition(), endingEvent);

        currentContextMap.put(structureLevel, null);
        return movement;
    }

    public void endContextWhenInWindow(ContextEndingEvent contextToEndLater) {
        this.contextToEndLater = contextToEndLater;
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {
        setStartingSequenceNumber(StructureLevel.PROJECT, carryOverContext.getSequenceFor(StructureLevel.PROJECT));
        setStartingSequenceNumber(StructureLevel.TASK, carryOverContext.getSequenceFor(StructureLevel.TASK));
        setStartingSequenceNumber(StructureLevel.INTENTION, carryOverContext.getSequenceFor(StructureLevel.INTENTION));

        currentContextMap.put(StructureLevel.PROJECT, carryOverContext.getContextFor(StructureLevel.PROJECT));
        currentContextMap.put(StructureLevel.TASK, carryOverContext.getContextFor(StructureLevel.TASK));
        currentContextMap.put(StructureLevel.INTENTION, carryOverContext.getContextFor(StructureLevel.INTENTION));

        if (carryOverContext.getContextToEndLater() != null) {
            ContextEndingEvent contextEndingEvent = carryOverContext.getContextToEndLater();
            LocalDateTime endingPosition = contextEndingEvent.getPosition();

            if ((from.isBefore(endingPosition) || from.isEqual(endingPosition)) && to.isAfter(endingPosition)) {
                endContext(contextEndingEvent);
            } else {
                contextToEndLater = contextEndingEvent;
            }
        }
    }

    public CarryOverContext getCarryOverContext() {
        CarryOverContext carryOverContext = new CarryOverContext();
        carryOverContext.addContext(StructureLevel.PROJECT, getCurrentContext(StructureLevel.PROJECT));
        carryOverContext.addContext(StructureLevel.TASK, getCurrentContext(StructureLevel.TASK));
        carryOverContext.addContext(StructureLevel.INTENTION, getCurrentContext(StructureLevel.INTENTION));

        carryOverContext.setContextToEndLater(contextToEndLater);

        return carryOverContext;
    }

    private RelativeSequence findOrCreateSequence(StructureLevel structureLevel) {
        RelativeSequence relativeSequence = this.currentSequenceNumbers.get(structureLevel);
        if (relativeSequence == null) {
            relativeSequence = new RelativeSequence(1);
            this.currentSequenceNumbers.put(structureLevel, relativeSequence);
        }
        return relativeSequence;
    }

    private void setStartingSequenceNumber(StructureLevel structureLevel, int startingSequence) {
        RelativeSequence relativeSequence = findOrCreateSequence(structureLevel);
        relativeSequence.reset(startingSequence);
    }

    public void finish() {
        //TODO is there a thing that needs doing here?  Sequencing?
    }

    public static final class CarryOverContext {

        private ContextEndingEvent contextToEndLater;
        Map<StructureLevel, ContextBeginningEvent> contextMap = new HashMap<>();

        void addContext(StructureLevel structureLevel, ContextBeginningEvent contextEvent) {
            contextMap.put(structureLevel, contextEvent);
        }

        void setContextToEndLater(ContextEndingEvent contextEndingEvent) {
            contextToEndLater = contextEndingEvent;
        }

        ContextEndingEvent getContextToEndLater() {
            return contextToEndLater;
        }

        ContextBeginningEvent getContextFor(StructureLevel structureLevel) {
            return contextMap.get(structureLevel);
        }

        int getSequenceFor(StructureLevel structureLevel) {
            ContextBeginningEvent contextBeginningEvent = contextMap.get(structureLevel);
            if (contextBeginningEvent != null) {
                return contextBeginningEvent.getRelativeSequence() + 1;
            } else {
                return 1;
            }
        }
    }
}
