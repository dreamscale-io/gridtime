package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
public class ContextBeginningEvent extends FlowFeature implements ContextChangeEvent {

    private LocalDateTime position;
    private int relativeSequence;

    private Context context;

    public ContextBeginningEvent() {
        context = new Context();
    }

    public ContextBeginningEvent(LocalDateTime position, StructureLevel structureLevel, UUID referenceId) {
        this.position = position;
        this.context = new Context();
        context.setStructureLevel(structureLevel);
        context.setId(referenceId);
    }

    public void setReferenceId(UUID id) {
        context.setId(id);
    }

    public void setStructureLevel(StructureLevel structureLevel) {
        context.setStructureLevel(structureLevel);
    }

    public void setDescription(String description) {
        context.setDescription(description);
    }

    public StructureLevel getStructureLevel() {
        return context.getStructureLevel();
    }

    public UUID getReferenceId() {
        return context.getId();
    }

    public String getName() {
        return context.getName();
    }

    public String getDescription() {
        return context.getDescription();
    }

    public void setName(String name) {
        context.setName(name);
    }
}
