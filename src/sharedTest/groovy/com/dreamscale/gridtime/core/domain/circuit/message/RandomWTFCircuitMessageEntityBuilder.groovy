package com.dreamscale.gridtime.core.domain.circuit.message


import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomWTFCircuitMessageEntityBuilder extends WTFFeedMessageEntity.WTFFeedMessageEntityBuilder {

    private WTFFeedMessageRepository wtfFeedMessageRepository

    RandomWTFCircuitMessageEntityBuilder(WTFFeedMessageRepository wtfFeedMessageRepository) {
        this.wtfFeedMessageRepository = wtfFeedMessageRepository
        id(aRandom.uuid())
                .circleId(aRandom.uuid())
                .torchieId(aRandom.uuid())
                .position(aRandom.localDateTimeInFuture())
                .messageType(CircuitMessageType.CIRCUIT_OPEN)
    }

    WTFFeedMessageEntity save() {
        wtfFeedMessageRepository.save(build())
    }

}
