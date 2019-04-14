package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import org.springframework.stereotype.Component;

@Component
public class SaveToPostgresSink implements SinkStrategy {


    @Override
    public void save(StoryTile storyTile) {
        //alright, all of these objects, I need a strategy to serialize all this to JSON
        //all the URIs should be populated now

        //Everything that needs to be saved is a FlowFeature, or a Details object

        //each layer mapper, needs to be able to rehydrate, so we can rebuild a frame
        //the json handling and mapping to objects should be centralized

        //the serializing to json should be serialized

        //so I can get a list of FlowFeatures to save
        //the details are subobjects within a FlowFeature that don't have an ID of their own, just a bag of props

        //extractFlowFeatures...

        //injectFlowFeatures...

    }
}
