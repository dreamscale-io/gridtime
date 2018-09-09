package com.dreamscale.htmflow.core;

import com.dreamscale.htmflow.api.batch.NewIFMBatch;
import lombok.experimental.Delegate;
import org.dreamscale.testsupport.RandomGenerator;

public class CoreARandom {

    public static final CoreARandom aRandom = new CoreARandom();

    @Delegate
    private RandomGenerator randomGenerator = new RandomGenerator();
    @Delegate
    private CoreRandomBuilderSupport coreRandomBuilderSupport = new CoreRandomBuilderSupport();

}
