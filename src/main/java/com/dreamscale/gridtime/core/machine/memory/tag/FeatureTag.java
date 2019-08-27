package com.dreamscale.gridtime.core.machine.memory.tag;

import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class FeatureTag<F extends FeatureReference> {

    LocalDateTime position;
    F feature;
    Tag tag;

    public String name() { return tag.name(); }

    public boolean isStart() {
        return tag instanceof StartTag;
    }

    public boolean isFinish() {
        return tag instanceof FinishTag;
    }
}
