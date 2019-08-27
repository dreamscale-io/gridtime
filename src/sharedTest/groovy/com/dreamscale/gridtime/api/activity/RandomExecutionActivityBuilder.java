package com.dreamscale.gridtime.api.activity;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomExecutionActivityBuilder extends NewExecutionActivity.NewExecutionActivityBuilder {

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
