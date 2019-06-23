package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.transform;

import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.search.FeatureSearchService;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ResolveFeaturesTransform implements TransformStrategy {

    @Override
    public void transform(FeaturePool featurePool, GridTile gridTile) {

        gridTile.finishAfterLoad();
        featurePool.resolveReferences();

    }


}
