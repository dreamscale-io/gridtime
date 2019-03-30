package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.context.IdeaFlowContextBeginningEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.IdeaFlowContextChangeEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.IdeaFlowContextEndingEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.IdeaFlowStructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.IdeaFlowMovementEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.RelativeSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdeaFlowContextMapper {

    private Map<IdeaFlowStructureLevel, IdeaFlowContextBeginningEvent> currentContextMap = new HashMap<>();
    private Map<IdeaFlowStructureLevel, RelativeSequence> currentSequenceNumbers = new HashMap<>();


    private RelativeSequence findOrCreateSequence(IdeaFlowStructureLevel structureLevel) {
        RelativeSequence relativeSequence = this.currentSequenceNumbers.get(structureLevel);
        if (relativeSequence == null) {
            relativeSequence = new RelativeSequence(1);
            this.currentSequenceNumbers.put(structureLevel, relativeSequence);
        }
        return relativeSequence;
    }


    public IdeaFlowMovementEvent beginContext(IdeaFlowContextBeginningEvent beginningEvent) {
        IdeaFlowMovementEvent movement = null;

        IdeaFlowContextBeginningEvent currentContext = currentContextMap.get(beginningEvent.getStructureLevel());

        if (currentContext == null) {
            this.currentContextMap.put(beginningEvent.getStructureLevel(), beginningEvent);
            movement = new IdeaFlowMovementEvent(beginningEvent.getPosition(), beginningEvent);
        } else {
            IdeaFlowStructureLevel structureLevel = beginningEvent.getStructureLevel();
            RelativeSequence sequence = findOrCreateSequence(structureLevel);
            int nextSequence = sequence.increment();
            beginningEvent.setRelativeSequence(nextSequence);

            movement = new IdeaFlowMovementEvent(beginningEvent.getPosition(), beginningEvent);

            currentContextMap.put(structureLevel, beginningEvent);
        }
        return movement;
    }

    public IdeaFlowMovementEvent endContext(IdeaFlowContextEndingEvent endingEvent) {
        IdeaFlowMovementEvent movement = null;

        IdeaFlowStructureLevel structureLevel = endingEvent.getStructureLevel();
        RelativeSequence sequence = findOrCreateSequence(structureLevel);
        int nextSequence = sequence.increment();
        endingEvent.setRelativeSequence(nextSequence);

        movement = new IdeaFlowMovementEvent(endingEvent.getPosition(), endingEvent);

        currentContextMap.put(structureLevel, null);
        return movement;
    }

    public IdeaFlowContextBeginningEvent lookupCurrentContext(IdeaFlowStructureLevel structureLevel) {
        return this.currentContextMap.get(structureLevel);
    }

    public void setStartingSequenceNumber(IdeaFlowStructureLevel structureLevel, int startingSequence) {
        RelativeSequence relativeSequence = findOrCreateSequence(structureLevel);
        relativeSequence.reset(startingSequence);
    }

    private void incrementRelativeSequence(IdeaFlowStructureLevel structureLevel) {
        RelativeSequence relativeSequence = findOrCreateSequence(structureLevel);
        relativeSequence.increment();
    }

    private void resetSequence(IdeaFlowStructureLevel structureLevel) {
        RelativeSequence relativeSequence = findOrCreateSequence(structureLevel);
        relativeSequence.reset(1);
    }


    public void initContextFromPriorContext(IdeaFlowContextChangeEvent latestContext) {

    }

    public int getCurrentSequenceNumber(IdeaFlowStructureLevel structureLevel) {
        RelativeSequence relativeSequence = findOrCreateSequence(structureLevel);
        return relativeSequence.getRelativeSequence();
    }

    public IdeaFlowContextBeginningEvent getCurrentContext(IdeaFlowStructureLevel structureLevel) {
        return currentContextMap.get(structureLevel);
    }

    public void initSequenceFromPriorContext(IdeaFlowStructureLevel structureLevel, int startingSequence) {
        setStartingSequenceNumber(structureLevel, startingSequence);
    }


    public List<IdeaFlowContextChangeEvent> getContextStructure() {
        return new ArrayList<>(currentContextMap.values());
    }


}
