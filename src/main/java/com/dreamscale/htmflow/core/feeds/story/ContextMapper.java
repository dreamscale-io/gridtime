package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;
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


    public MovementEvent changeContext(ContextChangeEvent contextEvent) {
        MovementEvent movement = null;

        if (this.currentContextMap.get(contextEvent.getStructureLevel()) == null) {
            initContext(contextEvent);
        } else {
            StructureLevel structureLevel = contextEvent.getStructureLevel();
            RelativeSequence sequence = findOrCreateSequence(structureLevel);
            int nextSequence = sequence.increment();
            contextEvent.setRelativeSequence(nextSequence);

            movement = new MovementEvent(contextEvent.getPosition(), contextEvent);

            if (contextEvent instanceof ContextBeginningEvent) {
                this.currentContextMap.put(structureLevel, (ContextBeginningEvent) contextEvent);
            }
        }
        return movement;
    }

    private void initContext(ContextChangeEvent contextEvent) {
        if (contextEvent instanceof ContextBeginningEvent) {
            this.currentContextMap.put(contextEvent.getStructureLevel(), (ContextBeginningEvent) contextEvent);
        }
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
