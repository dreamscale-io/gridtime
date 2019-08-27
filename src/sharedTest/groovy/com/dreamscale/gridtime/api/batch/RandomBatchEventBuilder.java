package com.dreamscale.gridtime.api.batch;

import com.dreamscale.gridtime.api.event.EventType;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomBatchEventBuilder extends NewBatchEvent.NewBatchEventBuilder {

    public RandomBatchEventBuilder() {
        super();
        comment(aRandom.text(30));
        type(EventType.NOTE);
        position(aRandom.localDateTime());
    }
}
