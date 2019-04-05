package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextStructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginning;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextEnding;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextSummary;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ChangeContext;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class FlowContextMapper {


    private Map<ContextStructureLevel, ContextBeginning> currentContextMap = new HashMap<>();
    private Map<ContextStructureLevel, RelativeSequence> currentSequenceNumbers = new HashMap<>();

    private final LocalDateTime from;
    private final LocalDateTime to;

    private ContextEnding contextToEndLater = null;

    public FlowContextMapper(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public ContextBeginning getCurrentContext(ContextStructureLevel structureLevel) {
        return currentContextMap.get(structureLevel);
    }

    public Movement beginContext(ContextBeginning beginningEvent) {
        Movement movement = null;

        ContextBeginning currentContext = currentContextMap.get(beginningEvent.getStructureLevel());

        if (currentContext == null) {
            this.currentContextMap.put(beginningEvent.getStructureLevel(), beginningEvent);
            movement = new ChangeContext(beginningEvent.getPosition(), beginningEvent);
        } else {
            ContextStructureLevel structureLevel = beginningEvent.getStructureLevel();
            RelativeSequence sequence = findOrCreateSequence(structureLevel);
            int nextSequence = sequence.increment();
            beginningEvent.setRelativeSequence(nextSequence);

            movement = new ChangeContext(beginningEvent.getPosition(), beginningEvent);

            currentContextMap.put(structureLevel, beginningEvent);
        }
        return movement;
    }

    public Movement endContext(ContextEnding endingEvent) {
        Movement movement = null;

        ContextStructureLevel structureLevel = endingEvent.getStructureLevel();
        RelativeSequence sequence = findOrCreateSequence(structureLevel);
        int nextSequence = sequence.increment();
        endingEvent.setRelativeSequence(nextSequence);

        movement = new ChangeContext(endingEvent.getPosition(), endingEvent);

        currentContextMap.put(structureLevel, null);
        return movement;
    }

    public void endContextWhenInWindow(ContextEnding contextToEndLater) {
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
            ContextEnding contextEnding = subContext.getContextToEndLater();
            LocalDateTime endingPosition = contextEnding.getPosition();

            if ((from.isBefore(endingPosition) || from.isEqual(endingPosition)) && to.isAfter(endingPosition)) {
                sideEffectMovement = endContext(contextEnding);

            } else {
                contextToEndLater = contextEnding;
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


        void addContext(ContextStructureLevel structureLevel, ContextBeginning contextEvent) {
            subContext.addKeyValue(structureLevel.name(), contextEvent);
        }

        void setContextToEndLater(ContextEnding contextEnding) {
            subContext.addKeyValue(CONTEXT_TO_END_LATER, contextEnding);
        }

        ContextEnding getContextToEndLater() {
            return (ContextEnding) subContext.getValue(CONTEXT_TO_END_LATER);
        }

        ContextBeginning getContextFor(ContextStructureLevel structureLevel) {
            return (ContextBeginning) subContext.getValue(structureLevel.name());
        }

        int getSequenceFor(ContextStructureLevel structureLevel) {
            ContextBeginning contextBeginning = getContextFor(structureLevel);
            if (contextBeginning != null) {
                return contextBeginning.getRelativeSequence() + 1;
            } else {
                return 1;
            }
        }

        public CarryOverContext toCarryOverContext() {
            return subContext;
        }
    }
}
