package com.dreamscale.gridtime.api.flow.activity;

import com.dreamscale.gridtime.api.flow.activity.NewModificationActivityDto;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomModificationActivityBuilder extends NewModificationActivityDto.NewModificationActivityDtoBuilder {

    public RandomModificationActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .modificationCount(aRandom.positiveInt());
    }
}
