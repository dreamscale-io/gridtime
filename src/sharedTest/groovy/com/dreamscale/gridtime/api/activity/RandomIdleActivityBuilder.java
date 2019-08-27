package com.dreamscale.gridtime.api.activity;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomIdleActivityBuilder extends NewIdleActivity.NewIdleActivityBuilder {

    public RandomIdleActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime());
    }
}
