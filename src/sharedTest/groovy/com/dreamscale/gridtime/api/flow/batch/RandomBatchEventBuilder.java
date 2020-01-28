package com.dreamscale.gridtime.api.flow.batch;

import com.dreamscale.gridtime.api.flow.event.EventType;
import com.dreamscale.gridtime.api.flow.batch.NewFlowBatchEventDto;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomBatchEventBuilder extends NewFlowBatchEventDto.NewFlowBatchEventDtoBuilder {

    public RandomBatchEventBuilder() {
        super();
        comment(aRandom.text(30));
        type(EventType.NOTE);
        position(aRandom.localDateTime());
    }
}
