package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.metrics.GridObject;
import com.dreamscale.htmflow.core.feeds.story.feature.metrics.GridObjectMetrics;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ContextChangeEvent extends FlowFeature implements GridObject {

    private Type eventType;
    private UUID referenceId;
    private ContextStructureLevel structureLevel;
    private String name;
    private String description;
    private LocalDateTime position;
    private int relativeSequence;
    private FinishStatus finishStatus;

    private ContextReference context;

    private GridObjectMetrics gridObjectMetrics = new GridObjectMetrics();


    public enum Type {
        BEGINNING,
        ENDING
    }

    public enum FinishStatus {
        SUCCESS, ABORT
    }
}
