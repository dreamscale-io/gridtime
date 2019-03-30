package com.dreamscale.htmflow.core.feeds.story.feature.context;

import java.time.LocalDateTime;
import java.util.UUID;

public interface IdeaFlowContextChangeEvent {

    UUID getReferenceId();
    LocalDateTime getPosition();
    IdeaFlowStructureLevel getStructureLevel();
    int getRelativeSequence();
    void setRelativeSequence(int sequence);
}
