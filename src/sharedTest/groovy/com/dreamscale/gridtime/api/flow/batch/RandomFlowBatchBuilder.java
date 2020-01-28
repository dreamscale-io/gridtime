package com.dreamscale.gridtime.api.flow.batch;

import com.dreamscale.gridtime.api.flow.batch.NewFlowBatchDto;

import static com.dreamscale.gridtime.core.CoreARandom.aRandom;

public class RandomFlowBatchBuilder extends NewFlowBatchDto.NewFlowBatchDtoBuilder {

    public RandomFlowBatchBuilder() {
        super();
        timeSent(aRandom.localDateTime())
                .editorActivity(aRandom.editorActivity().build())
                .executionActivity(aRandom.executionActivity().build())
                .modificationActivity(aRandom.modificationActivity().build())
                .idleActivity(aRandom.idleActivity().build())
                .externalActivity(aRandom.externalActivity().build())
                .event(aRandom.event().build());
    }
}
