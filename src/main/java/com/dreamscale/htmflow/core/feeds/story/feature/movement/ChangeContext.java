package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.context.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
public class ChangeContext extends Movement {

    private Context changingContext;
    private EventType eventType;

    public ChangeContext(LocalDateTime moment, ContextChangeEvent contextChangeEvent) {
        super(moment, FlowObjectType.MOVEMENT_CHANGE_CONTEXT);

        this.changingContext = contextChangeEvent.getContext();

        if (contextChangeEvent instanceof ContextBeginningEvent) {
            eventType = EventType.CONTEXT_BEGINNING;
        } else {
            eventType = EventType.CONTEXT_ENDING;
        }
    }

    public ChangeContext() {
        super(FlowObjectType.MOVEMENT_CHANGE_CONTEXT);
    }

    @JsonIgnore
    public StructureLevel getStructureLevel() {
        return changingContext.getStructureLevel();
    }

    @JsonIgnore
    public UUID getObjectId() {
        return changingContext.getObjectId();
    }

    @JsonIgnore
    public String getDescription() {
        return changingContext.getDescription();
    }

    public enum EventType {
        CONTEXT_BEGINNING,
        CONTEXT_ENDING,
    }

}
