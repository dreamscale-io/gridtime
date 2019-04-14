package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

import java.util.List;

@Getter
public class Box extends FlowFeature {

    private final String boxName;

    public Box(String boxName) {
        this.boxName = boxName;
    }

}
