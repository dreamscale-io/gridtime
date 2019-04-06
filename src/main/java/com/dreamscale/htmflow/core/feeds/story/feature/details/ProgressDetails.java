package com.dreamscale.htmflow.core.feeds.story.feature.details;

public class ProgressDetails implements Details {

    String type;

    public ProgressDetails(Type type) {
        this.type = type.name();
    }

    public enum Type {
        LEARNING,
        PROGRESS
    }
}
