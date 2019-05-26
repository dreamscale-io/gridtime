package com.dreamscale.htmflow.core.feeds.pool.feature;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.pool.FeatureType;
import com.dreamscale.htmflow.core.feeds.pool.GridFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.mapper.SearchKeyMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class Box implements GridFeature {

    private String boxName;

    public Box(String boxName) {
        this.boxName = boxName;
    }

    @Override
    public String toSearchKey() {
        return SearchKeyMapper.createBoxKey(boxName);
    }
}
