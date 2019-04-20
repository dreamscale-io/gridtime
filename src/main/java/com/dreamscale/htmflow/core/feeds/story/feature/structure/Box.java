package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class Box extends FlowFeature {

    private final String boxName;

    public Box(String boxName) {
        this.boxName = boxName;
    }

}
