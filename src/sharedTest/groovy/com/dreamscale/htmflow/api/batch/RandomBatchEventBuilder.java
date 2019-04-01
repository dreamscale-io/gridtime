package com.dreamscale.htmflow.api.batch;

import com.dreamscale.htmflow.api.event.EventType;

import static com.dreamscale.htmflow.core.CoreARandom.aRandom;

public class RandomBatchEventBuilder extends NewBatchEvent.NewBatchEventBuilder {

    public RandomBatchEventBuilder() {
        super();
        comment(aRandom.text(30));
        type(EventType.NOTE);
        position(aRandom.localDateTime());
    }
}
