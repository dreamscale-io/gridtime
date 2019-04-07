package com.dreamscale.htmflow.core.feeds.story.feature;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public abstract class FlowFeature {

    private String uri;
    private String relativePath;
    private UUID id;
}
