package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
        super(FlowObjectType.CONTEXT_BEGINNING_EVENT);

        context = new Context();
    }

    public ContextBeginningEvent(LocalDateTime position, StructureLevel structureLevel, UUID referenceId) {
        super(FlowObjectType.CONTEXT_BEGINNING_EVENT);

        this.position = position;
        this.context = new Context();
        context.setStructureLevel(structureLevel);
        context.setObjectId(referenceId);
    }

    public void setContextId(UUID id) {
        context.setObjectId(id);
    }

    public void setStructureLevel(StructureLevel structureLevel) {
        context.setStructureLevel(structureLevel);
    }

    public void setDescription(String description) {
        context.setDescription(description);
    }

    @JsonIgnore
    public StructureLevel getStructureLevel() {
        return context.getStructureLevel();
    }

    @JsonIgnore
    public UUID getContextId() {
        return context.getObjectId();
    }

    @JsonIgnore
    public String getDescription() {
        return context.getDescription();
    }


}
