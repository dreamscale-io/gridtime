package com.dreamscale.gridtime.core.domain.journal


import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomTaskEntityBuilder extends TaskEntity.TaskEntityBuilder {

    private TaskRepository taskRepository

    RandomTaskEntityBuilder(TaskRepository taskRepository) {
        this.taskRepository = taskRepository
        id(aRandom.uuid())
                .name(aRandom.text(10))
                .description(aRandom.text(30))
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
