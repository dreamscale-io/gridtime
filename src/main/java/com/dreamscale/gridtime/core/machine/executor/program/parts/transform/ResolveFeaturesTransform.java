package com.dreamscale.gridtime.core.machine.executor.program.parts.transform;

import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import org.springframework.stereotype.Component;

@Component
public class ResolveFeaturesTransform implements TransformStrategy {

    @Override
    public void transform(TorchieState torchieState) {

        torchieState.getActiveTile().finishAfterLoad();
        torchieState.resolveFeatureReferences();

    }


}
