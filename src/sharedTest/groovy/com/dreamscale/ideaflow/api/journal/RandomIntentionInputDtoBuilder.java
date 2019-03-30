package com.dreamscale.ideaflow.api.journal;

import com.dreamscale.ideaflow.core.domain.TaskEntity;

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom;

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
