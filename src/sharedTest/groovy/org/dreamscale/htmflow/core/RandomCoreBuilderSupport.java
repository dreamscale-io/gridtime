package org.dreamscale.htmflow.core;

import org.dreamscale.htmflow.core.context.domain.RandomProjectEntityBuilder;

public class RandomCoreBuilderSupport {

    public RandomProjectEntityBuilder projectEntity() {
        return new RandomProjectEntityBuilder();
    }

}
