package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.domain.tile.StoryTileEntity;
import com.dreamscale.htmflow.core.domain.tile.StoryTileRepository;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.feature.StoryTileModel;
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

        String storyTileAsJson = toJson(storyTileModel);

        StoryTileEntity storyTileEntity = new StoryTileEntity();
        storyTileEntity.setId(UUID.randomUUID());
        storyTileEntity.setTorchieId(torchieId);
        storyTileEntity.setUri(storyTileModel.getTileUri());
        storyTileEntity.setZoomLevel(storyTileModel.getZoomLevel().name());

        GeometryClock.Coords coordinates = storyTileModel.getTileCoordinates();

        storyTileEntity.setClockPosition(coordinates.getClockTime());
        storyTileEntity.setYear(coordinates.getYear());
        storyTileEntity.setBlock(coordinates.getBlock());
        storyTileEntity.setWeeksIntoBlock(coordinates.getWeeksIntoBlock());
        storyTileEntity.setWeeksIntoYear(coordinates.getWeeksIntoYear());
        storyTileEntity.setDaysIntoWeek(coordinates.getDaysIntoWeek());
        storyTileEntity.setFourHourSteps(coordinates.getFourHourSteps());
        storyTileEntity.setTwentyMinuteSteps(coordinates.getTwentyMinuteSteps());

        storyTileEntity.setJsonTile(storyTileAsJson);

        storyTileRepository.save(storyTileEntity);


    }


    public String toJson(StoryTileModel model) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(model);
        } catch (JsonProcessingException ex) {
            log.error("Failed to convert StoryTileModel into json", ex);
            return "";
        }
    }
}
