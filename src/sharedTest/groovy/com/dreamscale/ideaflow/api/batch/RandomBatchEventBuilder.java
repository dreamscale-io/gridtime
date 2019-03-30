package com.dreamscale.ideaflow.api.batch;

import com.dreamscale.ideaflow.api.event.EventType;

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom;

public class RandomBatchEventBuilder extends NewBatchEvent.NewBatchEventBuilder {

    public RandomBatchEventBuilder() {
        super();
        comment(aRandom.text(30));
        type(EventType.NOTE);
        position(aRandom.localDateTime());
    }
}
