package com.dreamscale.gridtime.api.activity;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomExternalActivityBuilder extends NewExternalActivity.NewExternalActivityBuilder {

    public RandomExternalActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .comment(aRandom.text(30));
    }
}
