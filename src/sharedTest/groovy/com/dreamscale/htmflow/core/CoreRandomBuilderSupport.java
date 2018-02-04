package com.dreamscale.htmflow.core;

import com.dreamscale.htmflow.core.domain.RandomProjectEntityBuilder;
import com.dreamscale.htmflow.core.domain.RandomTaskEntityBuilder;

public class CoreRandomBuilderSupport {

    public RandomProjectEntityBuilder projectEntity() {
        return new RandomProjectEntityBuilder();
    }

    public RandomTaskEntityBuilder taskEntity() {
        return new RandomTaskEntityBuilder();
    }
}
