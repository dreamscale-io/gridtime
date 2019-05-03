package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Box extends FlowFeature {

    private String boxName;

    public Box(String boxName) {
        this();
        this.boxName = boxName;
    }

    public Box() {
        super(FlowObjectType.BOX);
    }

    public void setObjectId(UUID objectId) {
        setId(objectId);
    }

    public UUID getObjectId() {
        return getId();
    }

}
