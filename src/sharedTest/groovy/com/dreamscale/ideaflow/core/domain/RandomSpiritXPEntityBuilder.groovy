package com.dreamscale.ideaflow.core.domain

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom

class RandomSpiritXPEntityBuilder extends SpiritXPEntity.SpiritXPEntityBuilder {

    private SpiritXPRepository spiritXpRepository

    RandomSpiritXPEntityBuilder(SpiritXPRepository spiritXpRepository) {
        this.spiritXpRepository = spiritXpRepository
        id(aRandom.uuid())
                .totalXp(aRandom.intBetween(100, 800))
                .spiritId(aRandom.uuid())
                .organizationId(aRandom.uuid())
    }


    SpiritXPEntity save() {
        spiritXpRepository.save(build())
    }

}
