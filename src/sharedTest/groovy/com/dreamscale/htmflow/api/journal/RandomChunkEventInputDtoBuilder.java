package com.dreamscale.htmflow.api.journal;

import com.dreamscale.htmflow.core.domain.TaskEntity;

import static com.dreamscale.htmflow.core.CoreARandom.aRandom;

public class RandomChunkEventInputDtoBuilder extends ChunkEventInputDto.ChunkEventInputDtoBuilder {

    public RandomChunkEventInputDtoBuilder() {
        super();
        description(aRandom.text(10))
                .projectId(aRandom.uuid())
                .taskId(aRandom.uuid());
    }

    public RandomChunkEventInputDtoBuilder forTask(TaskEntity task) {
        projectId(task.getProjectId());
        taskId(task.getId());
        return this;
    }
}
