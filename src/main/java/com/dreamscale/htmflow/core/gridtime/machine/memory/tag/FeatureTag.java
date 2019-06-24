package com.dreamscale.htmflow.core.gridtime.machine.memory.tag;

import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FeatureTag<F extends FeatureReference> {
    F feature;
    Tag tag;

    public boolean isStart() {
        return tag instanceof StartTag;
    }

    public boolean isFinish() {
        return tag instanceof FinishTag;
    }
}
