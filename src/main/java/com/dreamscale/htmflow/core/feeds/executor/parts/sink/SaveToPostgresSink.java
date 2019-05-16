package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.domain.tile.*;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.StoryTileModel;
import com.dreamscale.htmflow.core.feeds.story.grid.CandleStick;
import com.dreamscale.htmflow.core.feeds.story.grid.FeatureMetrics;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGridModel;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryTileSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class SaveToPostgresSink implements SinkStrategy {

    @Autowired
    StoryTileRepository storyTileRepository;

    @Autowired
    StoryGridSummaryRepository storyGridSummaryRepository;

    @Autowired
    StoryGridMetricsRepository storyGridMetricsRepository;

    @Override
    public void save(UUID torchieId, StoryTile storyTile) {

        storyTile.finishAfterLoad();

        StoryTileModel storyTileModel = storyTile.extractStoryTileModel();

        StoryTileEntity storyTileEntity = createStoryTileEntity(torchieId, storyTileModel);
        storyTileRepository.save(storyTileEntity);

        log.debug(storyTileEntity.getJsonTile());

        StoryGridSummaryEntity summaryEntity = createStoryGridSummaryEntity(torchieId, storyTileEntity.getId(), storyTileModel.getStoryTileSummary());
        storyGridSummaryRepository.save(summaryEntity);

        List<StoryGridMetricsEntity> storyGridEntities = createStoryGridMetricEntities(torchieId, storyTileEntity.getId(), storyTileModel.getStoryGrid());
        storyGridMetricsRepository.save(storyGridEntities);

        log.debug("Saved tile: "+storyTileEntity.getUri());


    }

    private StoryGridSummaryEntity createStoryGridSummaryEntity(UUID torchieId, UUID tileId, StoryTileSummary storyTileSummary) {

        StoryGridSummaryEntity summaryEntity = new StoryGridSummaryEntity();
        summaryEntity.setId(UUID.randomUUID());
        summaryEntity.setTorchieId(torchieId);
        summaryEntity.setTileId(tileId);

        summaryEntity.setAverageMood(storyTileSummary.getAvgMood());
        summaryEntity.setPercentLearning(storyTileSummary.getPercentLearning());
        summaryEntity.setPercentProgress(storyTileSummary.getPercentProgress());
        summaryEntity.setPercentTroubleshooting(storyTileSummary.getPercentTroubleshooting());
        summaryEntity.setPercentPairing(storyTileSummary.getPercentPairing());

        summaryEntity.setBoxesVisited(storyTileSummary.getBoxesVisited());
        summaryEntity.setLocationsVisited(storyTileSummary.getLocationsVisited());
        summaryEntity.setBridgesVisited(storyTileSummary.getBridgesVisited());
        summaryEntity.setTraversalsVisited(storyTileSummary.getTraversalsVisited());
        summaryEntity.setBubblesVisited(storyTileSummary.getBubblesVisited());

        return summaryEntity;
    }

    private List<StoryGridMetricsEntity> createStoryGridMetricEntities(UUID torchieId, UUID tileId, StoryGridModel storyGridModel) {

        List<StoryGridMetricsEntity> gridMetrics = new ArrayList<>();

        Set<String> uris = storyGridModel.getAllFeaturesVisited();

        for (String uri : uris) {
            FeatureMetrics metrics = storyGridModel.getFeatureMetricTotals().getFeatureMetrics(uri);

            Map<CandleType, CandleStick> candleMap = metrics.getMetrics().getCandleMap();

            for (CandleType candleType : candleMap.keySet()) {
                CandleStick candleStick = candleMap.get(candleType);

                StoryGridMetricsEntity metricsEntity = new StoryGridMetricsEntity();
                metricsEntity.setId(UUID.randomUUID());
                metricsEntity.setFeatureUri(uri);
                metricsEntity.setTileId(tileId);
                metricsEntity.setTorchieId(torchieId);

                metricsEntity.setCandleType(candleType.name());
                metricsEntity.setSampleCount(candleStick.getSampleCount());
                metricsEntity.setAvg(candleStick.getAvg());
                metricsEntity.setTotal(candleStick.getTotal());
                metricsEntity.setStddev(candleStick.getStddev());
                metricsEntity.setMin(candleStick.getMin());
                metricsEntity.setMax(candleStick.getMax());

                gridMetrics.add(metricsEntity);
            }
        }

        return gridMetrics;
    }


    private StoryTileEntity createStoryTileEntity(UUID torchieId, StoryTileModel storyTileModel) {

        StoryTileEntity storyTileEntity = new StoryTileEntity();

        storyTileEntity.setId(UUID.randomUUID());
        storyTileEntity.setTorchieId(torchieId);
        storyTileEntity.setUri(storyTileModel.getTileUri());
        storyTileEntity.setZoomLevel(storyTileModel.getZoomLevel().name());

        GeometryClock.Coords coordinates = storyTileModel.getTileCoordinates();

        storyTileEntity.setClockPosition(coordinates.getClockTime());
        storyTileEntity.setDreamTime(coordinates.formatDreamTime());

        storyTileEntity.setYear(coordinates.getYear());
        storyTileEntity.setBlock(coordinates.getBlock());
        storyTileEntity.setWeeksIntoBlock(coordinates.getWeeksIntoBlock());
        storyTileEntity.setWeeksIntoYear(coordinates.getWeeksIntoYear());
        storyTileEntity.setDaysIntoWeek(coordinates.getDaysIntoWeek());
        storyTileEntity.setFourHourSteps(coordinates.getFourhours());
        storyTileEntity.setTwentyMinuteSteps(coordinates.getTwenties());


        String storyTileAsJson = JSONTransformer.toJson(storyTileModel);
        String summaryAsJson = JSONTransformer.toJson(storyTileModel.getStoryTileSummary());

        storyTileEntity.setJsonTile(storyTileAsJson);
        storyTileEntity.setJsonTileSummary(summaryAsJson);

        return storyTileEntity;
    }

}
