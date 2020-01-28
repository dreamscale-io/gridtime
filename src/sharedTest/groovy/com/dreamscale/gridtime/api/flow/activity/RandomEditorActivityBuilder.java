package com.dreamscale.gridtime.api.flow.activity;

import com.dreamscale.gridtime.api.flow.activity.NewEditorActivityDto;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomEditorActivityBuilder extends NewEditorActivityDto.NewEditorActivityDtoBuilder {

    public RandomEditorActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .filePath(aRandom.filePath())
                .isModified(aRandom.coinFlip());
    }
}
