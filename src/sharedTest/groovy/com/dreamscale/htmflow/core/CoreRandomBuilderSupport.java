package com.dreamscale.htmflow.core;

import com.dreamscale.htmflow.api.journal.RandomChunkEventInputDtoBuilder;
import com.dreamscale.htmflow.core.domain.RandomOrganizationEntityBuilder;
import com.dreamscale.htmflow.core.domain.RandomProjectEntityBuilder;
import com.dreamscale.htmflow.core.domain.RandomTaskEntityBuilder;

public class CoreRandomBuilderSupport {

    public RandomProjectEntityBuilder projectEntity() {
        return new RandomProjectEntityBuilder();
    }

    public RandomOrganizationEntityBuilder organizationEntity() {
        return new RandomOrganizationEntityBuilder();
    }

    public RandomTaskEntityBuilder taskEntity() {
        return new RandomTaskEntityBuilder();
    }

    public RandomChunkEventInputDtoBuilder chunkEventInputDto() {
        return new RandomChunkEventInputDtoBuilder();
    }
}
