package com.dreamscale.gridtime.core.domain.journal

import com.dreamscale.gridtime.core.domain.flow.FinishStatus

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomIntentionEntityBuilder extends IntentionEntity.IntentionEntityBuilder {

    private IntentionRepository intentionRepository

    RandomIntentionEntityBuilder(IntentionRepository intentionRepository) {
        this.intentionRepository = intentionRepository
        id(aRandom.uuid())
                .description(aRandom.text(10))
                .position(aRandom.localDateTime())
                .flameRating(aRandom.intBetween(-5, 5))
                .linked(false)
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
