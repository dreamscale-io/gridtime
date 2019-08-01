package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform;

import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import org.springframework.stereotype.Component;

@Component
public class ResolveFeaturesTransform implements TransformStrategy {

    @Override
    public void transform(TorchieState torchieState) {

        torchieState.getActiveTile().finishAfterLoad();
        torchieState.resolveFeatureReferences();

    }


}
