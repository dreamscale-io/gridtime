package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.domain.tile.*;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.StoryTileModel;
import com.dreamscale.htmflow.core.feeds.story.grid.CandleStick;
import com.dreamscale.htmflow.core.feeds.story.grid.FeatureMetrics;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGridModel;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGridSummary;
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

        StoryGridSummaryEntity summaryEntity = createStoryGridSummaryEntity(torchieId, storyTileEntity.getId(), storyTileModel.getStoryGridModel().getStoryGridSummary());
        storyGridSummaryRepository.save(summaryEntity);

        List<StoryGridMetricsEntity> storyGridEntities = createStoryGridMetricEntities(torchieId, storyTileEntity.getId(), storyTileModel.getStoryGridModel());
        storyGridMetricsRepository.save(storyGridEntities);

        log.debug("Saved tile: "+storyTileEntity.getUri());

    }

    private StoryGridSummaryEntity createStoryGridSummaryEntity(UUID torchieId, UUID tileId, StoryGridSummary storyGridSummary) {

        StoryGridSummaryEntity summaryEntity = new StoryGridSummaryEntity();
        summaryEntity.setId(UUID.randomUUID());
        summaryEntity.setTorchieId(torchieId);
        summaryEntity.setTileId(tileId);

        summaryEntity.setAverageMood(storyGridSummary.getAvgMood());
        summaryEntity.setPercentLearning(storyGridSummary.getPercentLearning());
        summaryEntity.setPercentProgress(storyGridSummary.getPercentProgress());
        summaryEntity.setPercentTroubleshooting(storyGridSummary.getPercentTroubleshooting());
        summaryEntity.setPercentPairing(storyGridSummary.getPercentPairing());

        summaryEntity.setBoxesVisited(storyGridSummary.getBoxesVisited());
        summaryEntity.setLocationsVisited(storyGridSummary.getLocationsVisited());
        summaryEntity.setBridgesVisited(storyGridSummary.getBridgesVisited());
        summaryEntity.setTraversalsVisited(storyGridSummary.getTraversalsVisited());
        summaryEntity.setBubblesVisited(storyGridSummary.getBubblesVisited());

        return summaryEntity;
    }

    private List<StoryGridMetricsEntity> createStoryGridMetricEntities(UUID torchieId, UUID tileId, StoryGridModel storyGridModel) {

        List<StoryGridMetricsEntity> gridMetrics = new ArrayList<>();

        Set<String> uris = storyGridModel.getAllFeaturesVisited();

        for (String uri : uris) {
            FeatureMetrics metrics = storyGridModel.getFeatureMetrics(uri);

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
        String summaryAsJson = JSONTransformer.toJson(storyTileModel.getStoryGridModel().getStoryGridSummary());

        storyTileEntity.setJsonTile(storyTileAsJson);
        storyTileEntity.setJsonTileSummary(summaryAsJson);

        return storyTileEntity;
    }

}
