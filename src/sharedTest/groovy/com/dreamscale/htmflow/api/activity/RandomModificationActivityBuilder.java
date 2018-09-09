package com.dreamscale.htmflow.api.activity;

import com.dreamscale.htmflow.api.activity.NewEditorActivity;
import com.dreamscale.htmflow.api.activity.NewModificationActivity;

import static com.dreamscale.htmflow.core.CoreARandom.aRandom;

public class RandomModificationActivityBuilder extends NewModificationActivity.NewModificationActivityBuilder {

    public RandomModificationActivityBuilder() {
        super();
        durationInSeconds(aRandom.duration().getSeconds())
                .endTime(aRandom.localDateTime())
                .modificationCount(aRandom.positiveInt());
    }
}
