package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextEndingEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.LayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.MovementEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.RelativeSequence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextMapper {

    private Map<StructureLevel, ContextBeginningEvent> currentContextMap = new HashMap<>();
    private Map<StructureLevel, RelativeSequence> currentSequenceNumbers = new HashMap<>();


    private RelativeSequence findOrCreateSequence(StructureLevel structureLevel) {
        RelativeSequence relativeSequence = this.currentSequenceNumbers.get(structureLevel);
        if (relativeSequence == null) {
            relativeSequence = new RelativeSequence(1);
            this.currentSequenceNumbers.put(structureLevel, relativeSequence);
        }
        return relativeSequence;
    }


    public MovementEvent beginContext(ContextBeginningEvent beginningEvent) {
        MovementEvent movement = null;

        ContextBeginningEvent currentContext = currentContextMap.get(beginningEvent.getStructureLevel());

        if (currentContext == null) {
            this.currentContextMap.put(beginningEvent.getStructureLevel(), beginningEvent);
            movement = new MovementEvent(beginningEvent.getPosition(), beginningEvent);
        } else {
            StructureLevel structureLevel = beginningEvent.getStructureLevel();
            RelativeSequence sequence = findOrCreateSequence(structureLevel);
            int nextSequence = sequence.increment();
            beginningEvent.setRelativeSequence(nextSequence);

            movement = new MovementEvent(beginningEvent.getPosition(), beginningEvent);

            currentContextMap.put(structureLevel, beginningEvent);
        }
        return movement;
    }

    public MovementEvent endContext(ContextEndingEvent endingEvent) {
        MovementEvent movement = null;

        StructureLevel structureLevel = endingEvent.getStructureLevel();
        RelativeSequence sequence = findOrCreateSequence(structureLevel);
        int nextSequence = sequence.increment();
        endingEvent.setRelativeSequence(nextSequence);

        movement = new MovementEvent(endingEvent.getPosition(), endingEvent);

        currentContextMap.put(structureLevel, null);
        return movement;
    }

    public ContextBeginningEvent lookupCurrentContext(StructureLevel structureLevel) {
        return this.currentContextMap.get(structureLevel);
    }

    public void setStartingSequenceNumber(StructureLevel structureLevel, int startingSequence) {
        RelativeSequence relativeSequence = findOrCreateSequence(structureLevel);
        relativeSequence.reset(startingSequence);
    }

    private void incrementRelativeSequence(StructureLevel structureLevel) {
        RelativeSequence relativeSequence = findOrCreateSequence(structureLevel);
        relativeSequence.increment();
    }

    private void resetSequence(StructureLevel structureLevel) {
        RelativeSequence relativeSequence = findOrCreateSequence(structureLevel);
        relativeSequence.reset(1);
    }


    public void initContextFromPriorContext(ContextChangeEvent latestContext) {

    }

    public int getCurrentSequenceNumber(StructureLevel structureLevel) {
        RelativeSequence relativeSequence = findOrCreateSequence(structureLevel);
        return relativeSequence.getRelativeSequence();
    }

    public ContextBeginningEvent getCurrentContext(StructureLevel structureLevel) {
        return currentContextMap.get(structureLevel);
    }

    public void initSequenceFromPriorContext(StructureLevel structureLevel, int startingSequence) {
        setStartingSequenceNumber(structureLevel, startingSequence);
    }


    public List<ContextChangeEvent> getContextStructure() {
        return new ArrayList<>(currentContextMap.values());
    }


}
