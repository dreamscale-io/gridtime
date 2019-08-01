package com.dreamscale.htmflow.core.gridtime.machine.memory;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.InputFeed;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;

import java.util.UUID;

public interface TorchieState {

    void gotoPosition(GeometryClock.GridTime toGridPosition);

    String getActiveTime();

    GridTile getActiveTile();

    void nextTile();

    <T extends Flowable> InputFeed<T> registerInputFeed(UUID memberId, FeedStrategyFactory.FeedType feedType, FeedStrategy<T> feedStrategy);

    <T extends Flowable> InputFeed<T> getInputFeed(FeedStrategyFactory.FeedType type);

    void resolveFeatureReferences();




}
