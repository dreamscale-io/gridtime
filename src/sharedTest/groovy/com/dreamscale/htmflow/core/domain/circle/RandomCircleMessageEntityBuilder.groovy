package com.dreamscale.htmflow.core.domain.circle

import com.dreamscale.htmflow.api.circle.CircleMessageType

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomCircleMessageEntityBuilder extends CircleMessageEntity.CircleMessageEntityBuilder {

    private CircleMessageRepository circleMessageRepository

    RandomCircleMessageEntityBuilder(CircleMessageRepository circleMessageRepository) {
        this.circleMessageRepository = circleMessageRepository
        id(aRandom.uuid())
                .circleId(aRandom.uuid())
                .torchieId(aRandom.uuid())
                .position(aRandom.localDateTimeInFuture())
                .messageType(CircleMessageType.CIRCLE_START)
    }

    CircleMessageEntity save() {
        circleMessageRepository.save(build())
    }

}
