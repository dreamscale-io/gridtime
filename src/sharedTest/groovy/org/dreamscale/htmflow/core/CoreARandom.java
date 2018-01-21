package org.dreamscale.htmflow.core;

import lombok.experimental.Delegate;
import org.dreamscale.testsupport.RandomGenerator;

public class CoreARandom {

    public static final CoreARandom aRandom = new CoreARandom();

    @Delegate
    private RandomGenerator randomGenerator = new RandomGenerator();
    @Delegate
    private RandomCoreBuilderSupport randomCoreBuilderSupport = new RandomCoreBuilderSupport();

}
