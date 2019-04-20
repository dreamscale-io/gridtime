package com.dreamscale.htmflow.core.domain.member

import com.dreamscale.htmflow.core.domain.member.SpiritXPEntity
import com.dreamscale.htmflow.core.domain.member.SpiritXPRepository

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomSpiritXPEntityBuilder extends SpiritXPEntity.SpiritXPEntityBuilder {

    private SpiritXPRepository spiritXpRepository

    RandomSpiritXPEntityBuilder(SpiritXPRepository spiritXpRepository) {
        this.spiritXpRepository = spiritXpRepository
        id(aRandom.uuid())
                .totalXp(aRandom.intBetween(100, 800))
                .torchieId(aRandom.uuid())
                .organizationId(aRandom.uuid())
    }


    SpiritXPEntity save() {
        spiritXpRepository.save(build())
    }

}