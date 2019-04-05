package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextStructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextEndingEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextSummary;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.Movement;
import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class FlowContextMapper {


    private Map<ContextStructureLevel, ContextBeginningEvent> currentContextMap = new HashMap<>();
    private Map<ContextStructureLevel, RelativeSequence> currentSequenceNumbers = new HashMap<>();

    private final LocalDateTime from;
    private final LocalDateTime to;

    private ContextEndingEvent contextToEndLater = null;

    public FlowContextMapper(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public ContextBeginningEvent getCurrentContext(ContextStructureLevel structureLevel) {
        return currentContextMap.get(structureLevel);
    }

    public Movement beginContext(ContextBeginningEvent beginningEvent) {
        Movement movement = null;

        ContextBeginningEvent currentContext = currentContextMap.get(beginningEvent.getStructureLevel());

        if (currentContext == null) {
            this.currentContextMap.put(beginningEvent.getStructureLevel(), beginningEvent);
            movement = new Movement(beginningEvent.getPosition(), beginningEvent);
        } else {
            ContextStructureLevel structureLevel = beginningEvent.getStructureLevel();
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

        ContextStructureLevel structureLevel = endingEvent.getStructureLevel();
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

    public Movement initFromCarryOverContext(CarryOverContext carryOverContext) {
        CarryOverSubContext subContext = new CarryOverSubContext(carryOverContext);

        Movement sideEffectMovement = null;

        setStartingSequenceNumber(ContextStructureLevel.PROJECT, subContext.getSequenceFor(ContextStructureLevel.PROJECT));
        setStartingSequenceNumber(ContextStructureLevel.TASK, subContext.getSequenceFor(ContextStructureLevel.TASK));
        setStartingSequenceNumber(ContextStructureLevel.INTENTION, subContext.getSequenceFor(ContextStructureLevel.INTENTION));

        currentContextMap.put(ContextStructureLevel.PROJECT, subContext.getContextFor(ContextStructureLevel.PROJECT));
        currentContextMap.put(ContextStructureLevel.TASK, subContext.getContextFor(ContextStructureLevel.TASK));
        currentContextMap.put(ContextStructureLevel.INTENTION, subContext.getContextFor(ContextStructureLevel.INTENTION));

        if (subContext.getContextToEndLater() != null) {
            ContextEndingEvent contextEndingEvent = subContext.getContextToEndLater();
            LocalDateTime endingPosition = contextEndingEvent.getPosition();

            if ((from.isBefore(endingPosition) || from.isEqual(endingPosition)) && to.isAfter(endingPosition)) {
                sideEffectMovement = endContext(contextEndingEvent);

            } else {
                contextToEndLater = contextEndingEvent;
            }
        }

        return sideEffectMovement;
    }

    public CarryOverContext getCarryOverContext() {
        CarryOverSubContext subContext = new CarryOverSubContext();
        subContext.addContext(ContextStructureLevel.PROJECT, getCurrentContext(ContextStructureLevel.PROJECT));
        subContext.addContext(ContextStructureLevel.TASK, getCurrentContext(ContextStructureLevel.TASK));
        subContext.addContext(ContextStructureLevel.INTENTION, getCurrentContext(ContextStructureLevel.INTENTION));

        subContext.setContextToEndLater(contextToEndLater);

        return subContext.toCarryOverContext();
    }

    private RelativeSequence findOrCreateSequence(ContextStructureLevel structureLevel) {
        RelativeSequence relativeSequence = this.currentSequenceNumbers.get(structureLevel);
        if (relativeSequence == null) {
            relativeSequence = new RelativeSequence(1);
            this.currentSequenceNumbers.put(structureLevel, relativeSequence);
        }
        return relativeSequence;
    }

    private void setStartingSequenceNumber(ContextStructureLevel structureLevel, int startingSequence) {
        RelativeSequence relativeSequence = findOrCreateSequence(structureLevel);
        relativeSequence.reset(startingSequence);
    }

    public void finish() {
        //TODO is there a thing that needs doing here?  Sequencing?
    }

    public ContextSummary getCurrentContextSummary() {
        ContextSummary contextSummary = new ContextSummary();
        contextSummary.setProjectContext(getCurrentContext(ContextStructureLevel.PROJECT));
        contextSummary.setTaskContext(getCurrentContext(ContextStructureLevel.TASK));
        contextSummary.setIntentionContext(getCurrentContext(ContextStructureLevel.INTENTION));

        return contextSummary;
    }

    public static final class CarryOverSubContext {

        private static final String SUBCONTEXT_NAME = "[FlowContextMapper]";
        private static final String CONTEXT_TO_END_LATER = "current.context.to.end.later";
        private final CarryOverContext subContext;

        public CarryOverSubContext() {
            subContext = new CarryOverContext( SUBCONTEXT_NAME);
        }

        public CarryOverSubContext(CarryOverContext mainContext) {
            subContext = mainContext.getSubContext(SUBCONTEXT_NAME);
        }


        void addContext(ContextStructureLevel structureLevel, ContextBeginningEvent contextEvent) {
            subContext.addKeyValue(structureLevel.name(), contextEvent);
        }

        void setContextToEndLater(ContextEndingEvent contextEndingEvent) {
            subContext.addKeyValue(CONTEXT_TO_END_LATER, contextEndingEvent);
        }

        ContextEndingEvent getContextToEndLater() {
            return (ContextEndingEvent) subContext.getValue(CONTEXT_TO_END_LATER);
        }

        ContextBeginningEvent getContextFor(ContextStructureLevel structureLevel) {
            return (ContextBeginningEvent) subContext.getValue(structureLevel.name());
        }

        int getSequenceFor(ContextStructureLevel structureLevel) {
            ContextBeginningEvent contextBeginningEvent = getContextFor(structureLevel);
            if (contextBeginningEvent != null) {
                return contextBeginningEvent.getRelativeSequence() + 1;
            } else {
                return 1;
            }
        }

        public CarryOverContext toCarryOverContext() {
            return subContext;
        }
    }
}
