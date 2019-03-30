package com.dreamscale.ideaflow.api.activity;

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom;

public class RandomIdleActivityBuilder extends NewIdleActivity.NewIdleActivityBuilder {

    public RandomIdleActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime());
    }
}
