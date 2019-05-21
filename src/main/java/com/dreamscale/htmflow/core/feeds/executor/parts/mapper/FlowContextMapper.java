package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;
import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.context.*;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ChangeContext;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.music.MusicClock;

import java.time.LocalDateTime;
import java.util.*;

public class FlowContextMapper {

    private final FeatureFactory featureFactory;
    private final LocalDateTime from;
    private final LocalDateTime to;
    private final MusicClock musicClock;

    private Map<StructureLevel, MusicalSequenceBeginning> currentContextMap = new HashMap<>();
    private Map<StructureLevel, RelativeSequence> currentSequenceNumbers = new HashMap<>();

    private MomentOfContext initialContext;
    private List<MomentOfContext> momentsOfContext = new ArrayList<>();

    private MusicalSequenceEnding contextToEndLater = null;


    public FlowContextMapper(FeatureFactory featureFactory, LocalDateTime from, LocalDateTime to) {
        this.featureFactory = featureFactory;
        this.musicClock = new MusicClock(from, to);
        this.from = from;
        this.to = to;
    }

    public MusicalSequenceBeginning getCurrentContext(StructureLevel structureLevel) {
        return currentContextMap.get(structureLevel);
    }

    public Movement beginContext(MusicalSequenceBeginning beginningEvent) {
        Movement movement = null;

        lookupAndAttachToContextObject(beginningEvent);

        MusicalSequenceBeginning existingContextAtLevel = currentContextMap.get(beginningEvent.getStructureLevel());

        if (existingContextAtLevel == null) {

            this.currentContextMap.put(beginningEvent.getStructureLevel(), beginningEvent);
            movement = new ChangeContext(beginningEvent.getPosition(), beginningEvent);
        } else {
            StructureLevel structureLevel = beginningEvent.getStructureLevel();
            RelativeSequence sequence = findOrCreateSequence(structureLevel);
            int nextSequence = sequence.next();
            beginningEvent.setRelativeSequence(nextSequence);

            movement = new ChangeContext(beginningEvent.getPosition(), beginningEvent);

            currentContextMap.put(structureLevel, beginningEvent);
        }

        if (beginningEvent.getStructureLevel().equals(StructureLevel.INTENTION)) {
            momentsOfContext.add(getCurrentContext());
        }
        return movement;
    }

    private void lookupAndAttachToContextObject(ContextChangeEvent event) {
        Context context = featureFactory.findOrCreateContext(event);
        event.setContext(context);
    }

    public Movement endContext(MusicalSequenceEnding endingEvent) {
        lookupAndAttachToContextObject(endingEvent);

        Movement movement = null;

        StructureLevel structureLevel = endingEvent.getStructureLevel();
        RelativeSequence sequence = findOrCreateSequence(structureLevel);
        int nextSequence = sequence.next();
        endingEvent.setRelativeSequence(nextSequence);

        movement = new ChangeContext(endingEvent.getPosition(), endingEvent);

        currentContextMap.put(structureLevel, null);
        return movement;
    }

    public void endContextWhenInWindow(MusicalSequenceEnding contextToEndLater) {
        this.contextToEndLater = contextToEndLater;
    }

    public Movement initFromCarryOverContext(CarryOverContext carryOverContext) {
        CarryOverSubContext subContext = new CarryOverSubContext(carryOverContext);

        Movement sideEffectMovement = null;

        setStartingSequenceNumber(StructureLevel.PROJECT, subContext.getSequenceFor(StructureLevel.PROJECT));
        setStartingSequenceNumber(StructureLevel.TASK, subContext.getSequenceFor(StructureLevel.TASK));
        setStartingSequenceNumber(StructureLevel.INTENTION, subContext.getSequenceFor(StructureLevel.INTENTION));

        currentContextMap.put(StructureLevel.PROJECT, subContext.getContextFor(StructureLevel.PROJECT));
        currentContextMap.put(StructureLevel.TASK, subContext.getContextFor(StructureLevel.TASK));
        currentContextMap.put(StructureLevel.INTENTION, subContext.getContextFor(StructureLevel.INTENTION));

        if (subContext.getContextToEndLater() != null) {
            MusicalSequenceEnding contextEnding = subContext.getContextToEndLater();
            LocalDateTime endingPosition = contextEnding.getPosition();

            if ((from.isBefore(endingPosition) || from.isEqual(endingPosition)) && to.isAfter(endingPosition)) {
                sideEffectMovement = endContext(contextEnding);

            } else {
                contextToEndLater = contextEnding;
            }
        }

        initialContext = getCurrentContext();

        return sideEffectMovement;
    }

    public CarryOverContext getCarryOverContext() {
        CarryOverSubContext subContext = new CarryOverSubContext();
        subContext.addContext(StructureLevel.PROJECT, getCurrentContext(StructureLevel.PROJECT));
        subContext.addContext(StructureLevel.TASK, getCurrentContext(StructureLevel.TASK));
        subContext.addContext(StructureLevel.INTENTION, getCurrentContext(StructureLevel.INTENTION));

        subContext.setContextToEndLater(contextToEndLater);

        return subContext.toCarryOverContext();
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


    public MomentOfContext getInitialContext() {
        return initialContext;
    }

    public MomentOfContext getCurrentContext() {

        MusicalSequenceBeginning projectContext = getCurrentContext(StructureLevel.PROJECT);
        MusicalSequenceBeginning taskContext = getCurrentContext(StructureLevel.TASK);
        MusicalSequenceBeginning intentionContext = getCurrentContext(StructureLevel.INTENTION);

        MomentOfContext momentOfContext = new MomentOfContext(musicClock, projectContext, taskContext, intentionContext);

        return momentOfContext;
    }

    public MomentOfContext getMomentOfContext(LocalDateTime moment) {
        MomentOfContext contextOfMoment = null;

        //iterate backwards, and find the first context summary that is before the moment
        for (int i = momentsOfContext.size() - 1; i >= 0; i--) {
            MomentOfContext momentOfContext = momentsOfContext.get(i);

            if (momentOfContext.getPosition().isBefore(moment)) {
                contextOfMoment = momentOfContext;
                break;
            }
        }

        if (contextOfMoment == null) {
            contextOfMoment = getCurrentContext();
        }

        return contextOfMoment;
    }

    public List<MomentOfContext> getAllContexts() {
        return momentsOfContext;
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


        void addContext(StructureLevel structureLevel, MusicalSequenceBeginning contextEvent) {
            subContext.saveFeature(structureLevel.name(), contextEvent);
        }

        void setContextToEndLater(MusicalSequenceEnding contextEnding) {
            subContext.saveFeature(CONTEXT_TO_END_LATER, contextEnding);
        }

        MusicalSequenceEnding getContextToEndLater() {
            return (MusicalSequenceEnding) subContext.getFeature(CONTEXT_TO_END_LATER);
        }

        MusicalSequenceBeginning getContextFor(StructureLevel structureLevel) {
            return (MusicalSequenceBeginning) subContext.getFeature(structureLevel.name());
        }

        int getSequenceFor(StructureLevel structureLevel) {
            MusicalSequenceBeginning contextBeginning = getContextFor(structureLevel);
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
