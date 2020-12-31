package com.dreamscale.gridtime.core.machine.executor.program.parts.transform;

import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResolveFeaturesTransform implements TransformStrategy {

    @Override
    public void transform(TorchieState torchieState) {
        torchieState.resolveFeatureReferences();
        torchieState.getActiveTile().finishAfterLoad();
    }


}
