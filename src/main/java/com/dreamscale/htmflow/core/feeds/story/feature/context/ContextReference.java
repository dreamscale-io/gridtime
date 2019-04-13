package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.metrics.GridObject;
import com.dreamscale.htmflow.core.feeds.story.feature.metrics.GridObjectMetrics;
import lombok.Getter;

import java.time.Duration;
import java.util.UUID;

@Getter
public class ContextReference extends FlowFeature implements GridObject {

    private UUID id;
    private ContextStructureLevel structureLevel;
    private String name;
    private String description;

    public ContextReference(ContextChangeEvent event) {
        this.id = event.getReferenceId();
        this.structureLevel = event.getStructureLevel();
        this.name = event.getName();
        this.description = event.getDescription();
    }

    private GridObjectMetrics gridObjectMetrics = new GridObjectMetrics();


    public void spendTime(Duration timeInLocation) {
        gridObjectMetrics.addVelocitySample(timeInLocation.getSeconds());
    }

    public void modify(int modificationCount) {
        gridObjectMetrics.addModificationSample(modificationCount);
    }
}
