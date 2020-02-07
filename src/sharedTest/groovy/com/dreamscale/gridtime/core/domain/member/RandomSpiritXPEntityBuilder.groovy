package com.dreamscale.gridtime.core.domain.member


import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomSpiritXPEntityBuilder extends SpiritXPEntity.SpiritXPEntityBuilder {

    private SpiritXPRepository spiritXpRepository

    RandomSpiritXPEntityBuilder(SpiritXPRepository spiritXpRepository) {
        this.spiritXpRepository = spiritXpRepository
        id(aRandom.uuid())
                .totalXp(aRandom.intBetween(100, 800))
                .memberId(aRandom.uuid())
                .organizationId(aRandom.uuid())
    }


    SpiritXPEntity save() {
        spiritXpRepository.save(build())
    }

}
