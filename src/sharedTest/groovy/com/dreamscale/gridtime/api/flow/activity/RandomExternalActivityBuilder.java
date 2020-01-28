package com.dreamscale.gridtime.api.flow.activity;

import com.dreamscale.gridtime.api.flow.activity.NewExternalActivityDto;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomExternalActivityBuilder extends NewExternalActivityDto.NewExternalActivityDtoBuilder {

    public RandomExternalActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .comment(aRandom.text(30));
    }
}
