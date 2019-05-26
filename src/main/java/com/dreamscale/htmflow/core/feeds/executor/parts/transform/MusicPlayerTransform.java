package com.dreamscale.htmflow.core.feeds.executor.parts.transform;

import com.dreamscale.htmflow.core.feeds.story.mapper.URIMapper;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;
import com.dreamscale.htmflow.core.feeds.story.grid.TileGridModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MusicPlayerTransform implements TransformStrategy {

    @Autowired
    URIMapper uriMapper;

    @Override
    public void transform(TileBuilder tileBuilder) {
        tileBuilder.play();

        TileGridModel storyGrid = tileBuilder.getTileGrid();

        uriMapper.populateAndSaveStoryGridUris(tileBuilder.getTileUri(), storyGrid);

    }



}
