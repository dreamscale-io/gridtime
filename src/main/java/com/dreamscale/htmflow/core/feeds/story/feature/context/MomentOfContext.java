package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class MomentOfContext extends FlowFeature {

    private Context projectContext;
    private Context taskContext;
    private Context intentionContext;

    private final LocalDateTime position;

    private MusicGeometryClock.Coords coordinates;

    public MomentOfContext(MusicGeometryClock internalClock, ContextBeginningEvent projectEvent,
                           ContextBeginningEvent taskEvent,
                           ContextBeginningEvent intentionEvent) {

        if (projectEvent != null) {
            this.projectContext = projectEvent.getContext();
        }

        if (taskEvent != null) {
            this.taskContext = taskEvent.getContext();
        }

        if (intentionEvent != null) {
            this.intentionContext = intentionEvent.getContext();
        }

        position = determinePosition(projectEvent, taskEvent, intentionEvent);
        if (position != null) {
            coordinates = internalClock.createCoords(position);
        }
    }

    private LocalDateTime determinePosition(ContextBeginningEvent projectEvent,
                                           ContextBeginningEvent taskEvent,
                                           ContextBeginningEvent intentionEvent) {
        LocalDateTime position = null;
        if (intentionEvent != null) {
            position = intentionEvent.getPosition();
        } else if (taskEvent != null) {
            position = taskEvent.getPosition();
        } else if (projectEvent != null) {
            position = projectEvent.getPosition();
        }
        return position;
    }

    public UUID getProjectId() {
        UUID projectId = null;
        if (projectContext != null) {
            projectId = projectContext.getId();
        }
        return projectId;
    }
}
