package com.dreamscale.htmflow.core.domain

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomTaskEntityBuilder extends TaskEntity.TaskEntityBuilder {

    RandomTaskEntityBuilder() {
        id(aRandom.uuid())
                .name(aRandom.text(10))
                .summary(aRandom.text(30))
                .externalId(aRandom.numberText(5))
                .projectId(aRandom.uuid())
                .organizationId(aRandom.uuid())
    }

    RandomTaskEntityBuilder forProject(ProjectEntity projectEntity) {
        projectId(projectEntity.getId())
        return this;
    }
}
