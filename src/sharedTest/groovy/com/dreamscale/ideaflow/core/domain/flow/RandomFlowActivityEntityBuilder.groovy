package com.dreamscale.ideaflow.core.domain.flow


import static com.dreamscale.ideaflow.core.CoreARandom.aRandom

class RandomFlowActivityEntityBuilder extends FlowActivityEntity.FlowActivityEntityBuilder {

    private FlowActivityRepository flowActivityRepository

    RandomFlowActivityEntityBuilder(FlowActivityRepository flowActivityRepository) {
        this.flowActivityRepository = flowActivityRepository
        id(aRandom.nextLong())
                .start(aRandom.localDateTime())
                .end(aRandom.localDateTimeInFuture())
                .activityType(FlowActivityType.Editor)
                .memberId(aRandom.uuid())
    }


    FlowActivityEntity save() {
        flowActivityRepository.save(build())
    }


}
