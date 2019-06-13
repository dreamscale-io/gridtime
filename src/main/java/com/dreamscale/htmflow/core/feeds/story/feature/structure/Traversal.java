package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.pool.GridFeature;
import com.dreamscale.htmflow.core.feeds.story.mapper.SearchKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class Traversal extends FlowFeature {

    private LocationInBox locationA;
    private LocationInBox locationB;

    public Traversal(LocationInBox locationA, LocationInBox locationB) {
        this();
        this.locationA = locationA;
        this.locationB = locationB;
    }

    public void setObjectId(UUID objectId) {
        setId(objectId);
    }

    public UUID getObjectId() {
        return getId();
    }

    public Traversal() {
        super(FlowObjectType.TRAVERSAL);
    }

    public String toKey() {
        return SearchKeyMapper.createLocationTraversalKey(locationA.toKey(), locationB.toKey());
    }
}
