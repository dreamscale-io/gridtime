package com.dreamscale.ideaflow.api.event;

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom;

public class RandomSnippetEventBuilder extends NewSnippetEvent.NewSnippetEventBuilder {

    public RandomSnippetEventBuilder() {
        super();
        comment(aRandom.text(30));
        source(aRandom.filePath());
        snippet(aRandom.text(200));
        position(aRandom.localDateTime());
    }
}
