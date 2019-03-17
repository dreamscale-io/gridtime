package com.dreamscale.htmflow.core.domain.flow


import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity
import com.dreamscale.htmflow.core.domain.flow.FlowActivityRepository
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

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
