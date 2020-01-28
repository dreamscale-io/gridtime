package com.dreamscale.gridtime.api.flow.activity;

import com.dreamscale.gridtime.api.flow.activity.NewIdleActivityDto;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomIdleActivityBuilder extends NewIdleActivityDto.NewIdleActivityDtoBuilder {

    public RandomIdleActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime());
    }
}
