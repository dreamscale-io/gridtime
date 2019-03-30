package com.dreamscale.ideaflow.api.activity;

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom;

public class RandomEditorActivityBuilder extends NewEditorActivity.NewEditorActivityBuilder {

    public RandomEditorActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .filePath(aRandom.filePath())
                .isModified(aRandom.coinFlip());
    }
}
