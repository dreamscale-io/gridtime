package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform;

import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;
import org.springframework.stereotype.Component;

@Component
public class ResolveFeaturesTransform implements TransformStrategy {

    @Override
    public void transform(FeaturePool featurePool) {

        featurePool.getActiveGridTile().finishAfterLoad();
        featurePool.resolveReferences();

    }


}