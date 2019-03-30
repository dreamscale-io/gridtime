package com.dreamscale.ideaflow.core.feeds.executor.parts.source;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Saves the query position for a feed, so that input data is paged into the system
 * without duplicating or skipping any rows
 */
@Getter
public class Bookmark {

    private LocalDateTime position;
    private Long sequenceNumber;

    public Bookmark(LocalDateTime position) {
        this.position = position;
        this.sequenceNumber = 0L;
    }

    public Bookmark(LocalDateTime position, Long sequenceNumber) {
        this.position = position;
        this.sequenceNumber = sequenceNumber;
    }

    public void nudgeForward() {
        if (this.sequenceNumber > 0) {
            this.sequenceNumber++;
        } else {
            position = position.plusSeconds(1);
        }
    }
}
