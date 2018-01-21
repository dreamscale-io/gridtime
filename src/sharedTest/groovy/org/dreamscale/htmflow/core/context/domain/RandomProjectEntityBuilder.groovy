package org.dreamscale.htmflow.core.context.domain

import static org.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomProjectEntityBuilder extends ProjectEntity.ProjectEntityBuilder {

    RandomProjectEntityBuilder() {
        id(aRandom.uuid())
                .name(aRandom.text(10))
                .externalId(aRandom.numberText(5))
    }

}
