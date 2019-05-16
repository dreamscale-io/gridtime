package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.feeds.story.music.MusicClock;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class MomentOfContext {


    private Context projectContext;
    private Context taskContext;
    private Context intentionContext;

    private LocalDateTime position;
    private MusicClock.Beat coordinates;

    public MomentOfContext(MusicClock internalClock, ContextBeginningEvent projectEvent,
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

        LocalDateTime position = determinePosition(projectEvent, taskEvent, intentionEvent);
        if (position != null) {
            this.position = position;
            coordinates = internalClock.createBeat(position);
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

    @JsonIgnore
    public UUID getProjectId() {
        UUID projectId = null;
        if (projectContext != null) {
            projectId = projectContext.getId();
        }
        return projectId;
    }
}
