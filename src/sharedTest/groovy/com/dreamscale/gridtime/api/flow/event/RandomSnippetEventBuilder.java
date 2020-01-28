package com.dreamscale.gridtime.api.flow.event;

import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomSnippetEventBuilder extends NewSnippetEventDto.NewSnippetEventDtoBuilder {

    public RandomSnippetEventBuilder() {
        super();
        source(SnippetSourceType.EDITOR);
        filePath(aRandom.filePath());
        lineNumber(aRandom.intBetween(1, 5000));
        snippet(aRandom.text(200));
        position(aRandom.localDateTime());
    }
}
