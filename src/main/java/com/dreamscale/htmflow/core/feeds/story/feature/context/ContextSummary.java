package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ContextSummary extends FlowFeature {

    private ContextChangeEvent projectContext;
    private ContextChangeEvent taskContext;
    private ContextChangeEvent intentionContext;

    private InnerGeometryClock.Coords coordinates;

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
}
