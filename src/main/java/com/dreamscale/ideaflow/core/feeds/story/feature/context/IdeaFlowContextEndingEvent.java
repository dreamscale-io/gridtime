package com.dreamscale.ideaflow.core.feeds.story.feature.context;

import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IdeaFlowContextEndingEvent implements IdeaFlowFeature, IdeaFlowContextChangeEvent {

    private UUID referenceId;
    private IdeaFlowStructureLevel structureLevel;
    private String name;
    private String description;
    private LocalDateTime position;
    private int relativeSequence;
    private FinishStatus finishStatus;

    public enum FinishStatus {
        SUCCESS, ABORT
    }
}
