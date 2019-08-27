package com.dreamscale.gridtime.api.activity;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomEditorActivityBuilder extends NewEditorActivity.NewEditorActivityBuilder {

    public RandomEditorActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .filePath(aRandom.filePath())
                .isModified(aRandom.coinFlip());
    }
}
