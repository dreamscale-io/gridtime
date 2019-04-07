package com.dreamscale.htmflow.core.domain.journal

import com.dreamscale.htmflow.core.domain.flow.FinishStatus
import com.dreamscale.htmflow.core.domain.journal.IntentionEntity
import com.dreamscale.htmflow.core.domain.journal.IntentionRepository

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomIntentionEntityBuilder extends IntentionEntity.IntentionEntityBuilder {

    private IntentionRepository intentionRepository

    RandomIntentionEntityBuilder(IntentionRepository intentionRepository) {
        this.intentionRepository = intentionRepository
        id(aRandom.uuid())
                .description(aRandom.text(10))
                .position(aRandom.localDateTime())
                .flameRating(aRandom.intBetween(-5, 5))
                .linked(aRandom.coinFlip())
                .finishStatus(FinishStatus.done.name())
                .finishTime(aRandom.localDateTimeInFuture())
                .memberId(aRandom.uuid())
                .taskId(aRandom.uuid())
                .projectId(aRandom.uuid())
                .organizationId(aRandom.uuid())
    }


    IntentionEntity save() {
        intentionRepository.save(build())
    }

}
