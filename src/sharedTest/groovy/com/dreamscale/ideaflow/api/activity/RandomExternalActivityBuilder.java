package com.dreamscale.ideaflow.api.activity;

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom;

public class RandomExternalActivityBuilder extends NewExternalActivity.NewExternalActivityBuilder {

    public RandomExternalActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .comment(aRandom.text(30));
    }
}
