package com.dreamscale.htmflow.api.event;

import com.dreamscale.htmflow.api.event.EventType;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;

import static com.dreamscale.htmflow.core.CoreARandom.aRandom;

public class RandomSnippetEventBuilder extends NewSnippetEvent.NewSnippetEventBuilder {

    public RandomSnippetEventBuilder() {
        super();
        comment(aRandom.text(30));
        source(aRandom.filePath());
        snippet(aRandom.text(200));
        position(aRandom.localDateTime());
    }
}
