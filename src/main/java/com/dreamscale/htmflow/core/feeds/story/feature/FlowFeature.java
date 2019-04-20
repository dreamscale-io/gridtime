package com.dreamscale.htmflow.core.feeds.story.feature;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
public abstract class FlowFeature {

    private String uri;
    private String relativePath;
    private UUID id;
    private FlowObjectType flowObjectType;

    public FlowFeature() {
        id = UUID.randomUUID();
    }

}
