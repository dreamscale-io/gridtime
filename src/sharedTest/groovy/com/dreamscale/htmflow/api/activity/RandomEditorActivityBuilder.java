package com.dreamscale.htmflow.api.activity;

import com.dreamscale.htmflow.api.activity.NewEditorActivity;

import static com.dreamscale.htmflow.core.CoreARandom.aRandom;

public class RandomEditorActivityBuilder extends NewEditorActivity.NewEditorActivityBuilder {

    public RandomEditorActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .filePath(aRandom.filePath())
                .isModified(aRandom.coinFlip());
    }
}
