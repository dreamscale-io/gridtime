package com.dreamscale.htmflow.api.journal;

import com.dreamscale.htmflow.core.domain.journal.TaskEntity;

import static com.dreamscale.htmflow.core.CoreARandom.aRandom;

public class RandomIntentionInputDtoBuilder extends IntentionInputDto.IntentionInputDtoBuilder {

    public RandomIntentionInputDtoBuilder() {
        super();
        description(aRandom.text(10))
                .projectId(aRandom.uuid())
                .taskId(aRandom.uuid());
    }

    public RandomIntentionInputDtoBuilder forTask(TaskEntity task) {
        projectId(task.getProjectId());
        taskId(task.getId());
        return this;
    }
}
