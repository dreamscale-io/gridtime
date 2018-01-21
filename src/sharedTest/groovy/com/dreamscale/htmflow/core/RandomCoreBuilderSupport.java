package com.dreamscale.htmflow.core;

import com.dreamscale.htmflow.core.context.domain.RandomProjectEntityBuilder;

public class RandomCoreBuilderSupport {

    public RandomProjectEntityBuilder projectEntity() {
        return new RandomProjectEntityBuilder();
    }

}
