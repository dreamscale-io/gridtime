package com.dreamscale.htmflow.core.feeds.executor.parts.transform;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.URIMapper;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGridModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MusicPlayerTransform implements FlowTransform {

    @Autowired
    URIMapper uriMapper;

    @Override
    public void transform(StoryTile storyTile) {
        storyTile.play();

        StoryGridModel storyGrid = storyTile.getStoryGrid();

        uriMapper.populateAndSaveStoryGridUris(storyTile.getTileUri(), storyGrid);

    }



}
