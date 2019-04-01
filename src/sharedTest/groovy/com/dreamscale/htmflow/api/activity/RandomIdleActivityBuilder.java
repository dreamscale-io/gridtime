package com.dreamscale.htmflow.api.activity;

import static com.dreamscale.htmflow.core.CoreARandom.aRandom;

public class RandomIdleActivityBuilder extends NewIdleActivity.NewIdleActivityBuilder {

    public RandomIdleActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime());
    }
}
