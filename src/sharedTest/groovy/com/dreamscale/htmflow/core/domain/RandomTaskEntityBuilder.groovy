package com.dreamscale.htmflow.core.domain

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomTaskEntityBuilder extends TaskEntity.TaskEntityBuilder {

    private TaskRepository taskRepository

    RandomTaskEntityBuilder(TaskRepository taskRepository) {
        this.taskRepository = taskRepository
        id(aRandom.uuid())
                .name(aRandom.text(10))
                .summary(aRandom.text(30))
                .externalId(aRandom.numberText(5))
                .projectId(aRandom.uuid())
                .organizationId(aRandom.uuid())
    }

    RandomTaskEntityBuilder forProject(ProjectEntity projectEntity) {
        projectId(projectEntity.getId())
        organizationId(projectEntity.getOrganizationId())
        return this
    }

    RandomTaskEntityBuilder forProjectAndName(ProjectEntity projectEntity, String name) {
        this.name(name)
        forProject(projectEntity)
    }

    TaskEntity save() {
        taskRepository.save(build())
    }

}
