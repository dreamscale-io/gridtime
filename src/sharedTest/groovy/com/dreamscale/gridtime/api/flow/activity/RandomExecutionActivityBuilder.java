package com.dreamscale.gridtime.api.flow.activity;

import com.dreamscale.gridtime.api.flow.activity.NewExecutionActivityDto;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomExecutionActivityBuilder extends NewExecutionActivityDto.NewExecutionActivityDtoBuilder {

    public RandomExecutionActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .executionTaskType(aRandom.text(5))
                .exitCode(aRandom.positiveInt())
                .processName(aRandom.text(10))
                .isDebug(aRandom.coinFlip());
    }
}
