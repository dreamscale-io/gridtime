package com.dreamscale.gridtime.api.activity;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomModificationActivityBuilder extends NewModificationActivity.NewModificationActivityBuilder {

    public RandomModificationActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .modificationCount(aRandom.positiveInt());
    }
}
