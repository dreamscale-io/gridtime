package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.domain.tile.StoryTileEntity;
import com.dreamscale.htmflow.core.domain.tile.StoryTileRepository;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.StoryTileModel;
import com.dreamscale.htmflow.core.feeds.story.StoryTileSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class SaveToPostgresSink implements SinkStrategy {

    @Autowired
    StoryTileRepository storyTileRepository;

    @Override
    public void save(UUID torchieId, StoryTile storyTile) {

        StoryTileModel storyTileModel = storyTile.extractStoryTileModel();
        StoryTileSummary storyTileSummary = storyTile.extractStoryTileSummary();

        String storyTileAsJson = toJson(storyTileModel);
        String summaryAsJson = toJson(storyTileSummary);

        StoryTileEntity storyTileEntity = new StoryTileEntity();
        storyTileEntity.setId(UUID.randomUUID());
        storyTileEntity.setTorchieId(torchieId);
        storyTileEntity.setUri(storyTileModel.getTileUri());
        storyTileEntity.setZoomLevel(storyTileModel.getZoomLevel().name());

        GeometryClock.StoryCoords coordinates = storyTileModel.getTileCoordinates();

        storyTileEntity.setClockPosition(coordinates.getClockTime());
        //storyTileEntity.setDreamtime(coordinates.formatDreamtime());

        storyTileEntity.setYear(coordinates.getYear());
        storyTileEntity.setBlock(coordinates.getBlock());
        storyTileEntity.setWeeksIntoBlock(coordinates.getWeeksIntoBlock());
        storyTileEntity.setWeeksIntoYear(coordinates.getWeeksIntoYear());
        storyTileEntity.setDaysIntoWeek(coordinates.getDaysIntoWeek());
        storyTileEntity.setFourHourSteps(coordinates.getFours());
        storyTileEntity.setTwentyMinuteSteps(coordinates.getTwenties());

        storyTileEntity.setJsonTile(storyTileAsJson);

        storyTileRepository.save(storyTileEntity);


    }


    public String toJson(Object model) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(model);
        } catch (JsonProcessingException ex) {
            log.error("Failed to convert StoryTileModel into json", ex);
            return "";
        }
    }
}
