package com.dreamscale.htmflow.api.activity;

import com.dreamscale.htmflow.api.activity.NewExternalActivity;
import com.dreamscale.htmflow.api.activity.NewIdleActivity;

import static com.dreamscale.htmflow.core.CoreARandom.aRandom;

public class RandomExternalActivityBuilder extends NewExternalActivity.NewExternalActivityBuilder {

    public RandomExternalActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .comment(aRandom.text(30));
    }
}
