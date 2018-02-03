package com.dreamscale.htmflow.core;

import com.dreamscale.htmflow.core.context.domain.RandomProjectEntityBuilder;
import com.dreamscale.htmflow.core.context.domain.RandomTaskEntityBuilder;

public class RandomCoreBuilderSupport {

    public RandomProjectEntityBuilder projectEntity() {
        return new RandomProjectEntityBuilder();
    }

    public RandomTaskEntityBuilder taskEntity() {
        return new RandomTaskEntityBuilder();
    }
}
