package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
public class Context extends FlowFeature  {

    private StructureLevel structureLevel;
    private String name;
    private String description;

    public Context() {
       super(FlowObjectType.CONTEXT);
    }

    public Context(StructureLevel structureLevel, String name, String description) {
        super(FlowObjectType.CONTEXT);
        this.structureLevel = structureLevel;
        this.name = name;
        this.description = description;
    }

    public void setObjectId(UUID objectId) {
        setId(objectId);
    }

    public UUID getObjectId() {
        return getId();
    }
}
