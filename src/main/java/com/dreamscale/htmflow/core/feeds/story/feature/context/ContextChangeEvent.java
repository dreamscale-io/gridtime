package com.dreamscale.htmflow.core.feeds.story.feature.context;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ContextChangeEvent {

    UUID getReferenceId();
    LocalDateTime getPosition();
    ContextStructureLevel getStructureLevel();
    int getRelativeSequence();
    void setRelativeSequence(int sequence);
}
