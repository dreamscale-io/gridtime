package com.dreamscale.htmflow.core.feeds.executor.parts.transform;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.URIMapper;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.grid.TileGridModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MusicPlayerTransform implements TransformStrategy {

    @Autowired
    URIMapper uriMapper;

    @Override
    public void transform(StoryTile storyTile) {
        storyTile.play();

        TileGridModel storyGrid = storyTile.getTileGrid();

        uriMapper.populateAndSaveStoryGridUris(storyTile.getTileUri(), storyGrid);

    }



}
