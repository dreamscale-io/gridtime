package com.dreamscale.ideaflow.api.activity;

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom;

public class RandomModificationActivityBuilder extends NewModificationActivity.NewModificationActivityBuilder {

    public RandomModificationActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .modificationCount(aRandom.positiveInt());
    }
}
