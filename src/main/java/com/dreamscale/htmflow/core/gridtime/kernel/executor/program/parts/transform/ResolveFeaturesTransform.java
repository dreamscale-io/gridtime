package com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.transform;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.GridTile;
import org.springframework.stereotype.Component;

@Component
public class ResolveFeaturesTransform implements TransformStrategy {

    @Override
    public void transform(FeaturePool featurePool, GridTile gridTile) {

        gridTile.finishAfterLoad();
        featurePool.resolveReferences();

    }


}
