package com.dreamscale.htmflow.core.feeds.pool.feature;

import com.dreamscale.htmflow.core.feeds.pool.GridFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel;
import com.dreamscale.htmflow.core.feeds.story.mapper.SearchKeyMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class Context implements GridFeature {

    private StructureLevel structureLevel;
    private UUID referenceId;
    private String description;

    @Override
    public String toSearchKey() {
        return SearchKeyMapper.createContextSearchKey(structureLevel, referenceId);
    }
}
