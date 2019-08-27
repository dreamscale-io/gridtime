package com.dreamscale.gridtime.api.event;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomSnippetEventBuilder extends NewSnippetEvent.NewSnippetEventBuilder {

    public RandomSnippetEventBuilder() {
        super();
        comment(aRandom.text(30));
        source(aRandom.filePath());
        snippet(aRandom.text(200));
        position(aRandom.localDateTime());
    }
}
