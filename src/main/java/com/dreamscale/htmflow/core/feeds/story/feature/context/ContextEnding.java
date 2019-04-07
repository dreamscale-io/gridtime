package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ContextEnding extends FlowFeature implements ContextChangeEvent {

    private UUID referenceId;
    private ContextStructureLevel structureLevel;
    private String name;
    private String description;
    private LocalDateTime position;
    private int relativeSequence;
    private FinishStatus finishStatus;

    public enum FinishStatus {
        SUCCESS, ABORT
    }
}
