package com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.sink;

import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class SaveToPostgresSink implements SinkStrategy {


    @Override
    public void save(UUID torchieId, GridTile gridTile) {

        //alright so first thing, I want to know what project, task, intention, this tile should be contributed toward
        //these should carry over from tile to tile... let's get that working first.

        //I'm going to add these to the tile summary level, for the most part, they will be consistent


        gridTile.finishAfterLoad();

        //TODO rewrite the loading to DB code

//        StoryTileModel storyTileModel = gridTile.extractStoryTileModel();
//
//        log.debug(storyTileModel.getStoryTileSummary().toString());
//
//        StoryTileEntity storyTileEntity = createStoryTileEntity(torchieId, storyTileModel);
//        storyTileRepository.save(storyTileEntity);
//
//        //make sure doesn't explode
//        StoryTileModel model = JSONTransformer.fromJson(storyTileEntity.getJsonTile(), StoryTileModel.class);
//
//        StoryGridSummaryEntity summaryEntity = createStoryGridSummaryEntity(torchieId, storyTileEntity.getId(), storyTileModel.getStoryTileSummary());
//        storyGridSummaryRepository.save(summaryEntity);
//
//        List<StoryGridMetricsEntity> storyGridEntities = createStoryGridMetricEntities(torchieId, storyTileEntity.getId(), storyTileModel.getStoryGrid());
//        storyGridMetricsRepository.save(storyGridEntities);
//
//        log.debug("Saved tile: "+storyTileEntity.getUri());


    }
//
//    private StoryGridSummaryEntity createStoryGridSummaryEntity(UUID torchieId, UUID tileId, StoryTileSummary storyTileSummary) {
//
//        StoryGridSummaryEntity summaryEntity = new StoryGridSummaryEntity();
//        summaryEntity.setId(UUID.randomUUID());
//        summaryEntity.setTorchieId(torchieId);
//        summaryEntity.setTileId(tileId);
//
//        summaryEntity.setAverageMood(storyTileSummary.getAvgMood());
//        summaryEntity.setPercentLearning(storyTileSummary.getPercentLearning());
//        summaryEntity.setPercentProgress(storyTileSummary.getPercentProgress());
//        summaryEntity.setPercentTroubleshooting(storyTileSummary.getPercentTroubleshooting());
//        summaryEntity.setPercentPairing(storyTileSummary.getPercentPairing());
//
//        summaryEntity.setBoxesVisited(storyTileSummary.getBoxesVisited());
//        summaryEntity.setLocationsVisited(storyTileSummary.getLocationsVisited());
//        summaryEntity.setBridgesVisited(storyTileSummary.getBridgesVisited());
//        summaryEntity.setTraversalsVisited(storyTileSummary.getTraversalsVisited());
//        summaryEntity.setBubblesVisited(storyTileSummary.getBubblesVisited());
//
//        return summaryEntity;
//    }
//
//    private List<StoryGridMetricsEntity> createStoryGridMetricEntities(UUID torchieId, UUID tileId, TileGridModel tileGridModel) {
//
//        List<StoryGridMetricsEntity> gridMetrics = new ArrayList<>();
//
//        Set<String> uris = tileGridModel.getAllFeaturesVisited();
//
//        for (String uri : uris) {
//            FeatureMetrics metrics = tileGridModel.getFeatureMetricTotals().getFeatureMetrics(uri);
//
//            Map<CandleType, CandleStick> candleMap = metrics.getMetrics().getCandleMap();
//
//            for (CandleType candleType : candleMap.keySet()) {
//                CandleStick candleStick = candleMap.get(candleType);
//
//                StoryGridMetricsEntity metricsEntity = new StoryGridMetricsEntity();
//                metricsEntity.setId(UUID.randomUUID());
//                metricsEntity.setFeatureUri(uri);
//                metricsEntity.setTileId(tileId);
//                metricsEntity.setTorchieId(torchieId);
//
//                metricsEntity.setCandleType(candleType.name());
//                metricsEntity.setSampleCount(candleStick.getSampleCount());
//                metricsEntity.setAvg(candleStick.getAvg());
//                metricsEntity.setTotal(candleStick.getTotal());
//                metricsEntity.setStddev(candleStick.getStddev());
//                metricsEntity.setMin(candleStick.getMin());
//                metricsEntity.setMax(candleStick.getMax());
//
//                gridMetrics.add(metricsEntity);
//            }
//        }
//
//        return gridMetrics;
//    }


//    private StoryTileEntity createStoryTileEntity(UUID torchieId, StoryTileModel storyTileModel) {
//
//        StoryTileEntity storyTileEntity = new StoryTileEntity();
//
//        storyTileEntity.setId(UUID.randomUUID());
//        storyTileEntity.setTorchieId(torchieId);
//        storyTileEntity.setUri(storyTileModel.getTileUri());
//        storyTileEntity.setZoomLevel(storyTileModel.getZoomLevel().name());
//
//        GeometryClock.Coords coordinates = storyTileModel.getTileCoordinates();
//
//        storyTileEntity.setClockPosition(coordinates.getStartTime());
//        storyTileEntity.setDreamTime(coordinates.formatGridTime());
//
//        storyTileEntity.setYear(coordinates.getYear());
//        storyTileEntity.setBlock(coordinates.getBlock());
//        storyTileEntity.setWeeksIntoBlock(coordinates.getWeeksIntoBlock());
//        storyTileEntity.setWeeksIntoYear(coordinates.getWeeksIntoYear());
//        storyTileEntity.setDaysIntoWeek(coordinates.getDaysIntoWeek());
//        storyTileEntity.setFourHourSteps(coordinates.getTwelves());
//        storyTileEntity.setTwentyMinuteSteps(coordinates.getTwenties());
//
//
//        String storyTileAsJson = JSONTransformer.toJson(storyTileModel);
//        String summaryAsJson = JSONTransformer.toJson(storyTileModel.getStoryTileSummary());
//
//        storyTileEntity.setJsonTile(storyTileAsJson);
//        storyTileEntity.setJsonTileSummary(summaryAsJson);
//
//        return storyTileEntity;
//    }

}
