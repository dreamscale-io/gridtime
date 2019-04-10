package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ContextSummary extends FlowFeature {

    private ContextChangeEvent projectContext;
    private ContextChangeEvent taskContext;
    private ContextChangeEvent intentionContext;

    private MusicGeometryClock.Coords coordinates;

    public LocalDateTime getPosition() {
        LocalDateTime position = null;
        if (intentionContext != null) {
            position = intentionContext.getPosition();
        } else if (taskContext != null) {
            position = taskContext.getPosition();
        } else if (projectContext != null) {
            position = projectContext.getPosition();
        }
        return position;
    }

    public UUID getProjectId() {
        return projectContext.getReferenceId();
    }
}
