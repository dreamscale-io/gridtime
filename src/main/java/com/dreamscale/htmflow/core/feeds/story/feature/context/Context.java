package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
public class Context extends FlowFeature  {

    private StructureLevel structureLevel;
    private String description;

    public Context() {
       super(FlowObjectType.CONTEXT);
    }

    public Context(StructureLevel structureLevel, String description) {
        super(FlowObjectType.CONTEXT);
        this.structureLevel = structureLevel;
        this.description = description;
    }

    public void setObjectId(UUID objectId) {
        setId(objectId);
    }

    public UUID getObjectId() {
        return getId();
    }

    public String toString() {
        return structureLevel.name() + ":"+description;
    }
}
