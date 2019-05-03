package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Traversal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class MoveToLocation extends Movement {

    private LocationInBox location;

    @JsonIgnore
    private Traversal traversal;

    public MoveToLocation(LocalDateTime moment, LocationInBox location, Traversal traversal) {
        super(moment, FlowObjectType.MOVEMENT_TO_LOCATION);
        this.location = location;
        this.traversal = traversal;
    }

    public MoveToLocation() {
        super(FlowObjectType.MOVEMENT_TO_LOCATION);
    }

}
