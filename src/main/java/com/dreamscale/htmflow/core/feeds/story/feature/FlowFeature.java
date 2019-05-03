package com.dreamscale.htmflow.core.feeds.story.feature;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.domain.tile.StaticObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextEndingEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.AuthorsBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.FeelsBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.WTFFrictionBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.LearningFrictionBand;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "flowObjectType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LocationInBox.class, name = FlowFeature.LOCATION),
        @JsonSubTypes.Type(value = Traversal.class, name = FlowFeature.TRAVERSAL),
        @JsonSubTypes.Type(value = Box.class, name = FlowFeature.BOX),
        @JsonSubTypes.Type(value = ThoughtBubble.class, name = FlowFeature.THOUGHT_BUBBLE),
        @JsonSubTypes.Type(value = Bridge.class, name = FlowFeature.BRIDGE),
        @JsonSubTypes.Type(value = Context.class, name = FlowFeature.CONTEXT),
        @JsonSubTypes.Type(value = ListOfFlowFeatures.class, name = FlowFeature.LIST_OF_FEATURES),
        @JsonSubTypes.Type(value = MoveToBox.class, name = FlowFeature.MOVE_TO_BOX),
        @JsonSubTypes.Type(value = MoveToLocation.class, name = FlowFeature.MOVE_TO_LOCATION),
        @JsonSubTypes.Type(value = MoveAcrossBridge.class, name = FlowFeature.MOVE_ACROSS_BRIDGE),
        @JsonSubTypes.Type(value = ExecuteThing.class, name = FlowFeature.EXECUTE_THING),
        @JsonSubTypes.Type(value = ChangeContext.class, name = FlowFeature.CHANGE_CONTEXT),
        @JsonSubTypes.Type(value = PostCircleMessage.class, name = FlowFeature.POST_CIRCLE_MESSAGE),
        @JsonSubTypes.Type(value = ContextBeginningEvent.class, name = FlowFeature.CONTEXT_BEGINNING_EVENT),
        @JsonSubTypes.Type(value = ContextEndingEvent.class, name = FlowFeature.CONTEXT_ENDING_EVENT),
        @JsonSubTypes.Type(value = AuthorsBand.class, name = FlowFeature.AUTHORS_BAND),
        @JsonSubTypes.Type(value = FeelsBand.class, name = FlowFeature.FEELS_BAND),
        @JsonSubTypes.Type(value = WTFFrictionBand.class, name = FlowFeature.WTF_FRICTION_BAND),
        @JsonSubTypes.Type(value = LearningFrictionBand.class, name = FlowFeature.LEARNING_FRICTION_BAND),


})

public abstract class FlowFeature {

    static final String LOCATION= "LOCATION";
    static final String TRAVERSAL= "TRAVERSAL";
    static final String BOX= "BOX";
    static final String THOUGHT_BUBBLE= "THOUGHT_BUBBLE";
    static final String BRIDGE= "BRIDGE";
    static final String LIST_OF_FEATURES="LIST_OF_FEATURES";

    static final String MOVE_TO_BOX= "MOVE_TO_BOX";
    static final String MOVE_TO_LOCATION= "MOVE_TO_LOCATION";
    static final String MOVE_ACROSS_BRIDGE= "MOVE_ACROSS_BRIDGE";
    static final String EXECUTE_THING= "EXECUTE_THING";
    static final String POST_CIRCLE_MESSAGE= "POST_CIRCLE_MESSAGE";
    static final String CHANGE_CONTEXT= "CHANGE_CONTEXT";

    static final String CONTEXT_BEGINNING_EVENT="CONTEXT_BEGINNING_EVENT";
    static final String CONTEXT_ENDING_EVENT="CONTEXT_ENDING_EVENT";
    static final String CONTEXT="CONTEXT";
    static final String AUTHORS_BAND = "AUTHORS_BAND";
    static final String FEELS_BAND = "FEELS_BAND";
    static final String WTF_FRICTION_BAND = "WTF_FRICTION_BAND";
    static final String LEARNING_FRICTION_BAND = "LEARNING_FRICTION_BAND";

    private String uri;
    private String relativePath;
    private FlowObjectType flowObjectType;

    @JsonIgnore
    private UUID id;

    public FlowFeature(FlowObjectType flowObjectType) {
        id = UUID.randomUUID();
        this.flowObjectType = flowObjectType;
    }

    @JsonIgnore
    public FlowObjectType getFlowObjectType() {
        return flowObjectType;
    }

}
