package com.dreamscale.gridtime.core.machine.memory;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.executor.circuit.now.Eye;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.gridtime.core.machine.memory.box.BoxResolver;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feed.InputFeed;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;

import java.util.List;
import java.util.UUID;

public interface TorchieState {

    UUID getTorchieId();

    UUID getOrganizationId();

    List<UUID> getTeamIds();

    void changeBoxConfiguration(BoxResolver boxResolver);

    void gotoPosition(GeometryClock.GridTime toGridPosition);

    GeometryClock.GridTime getActiveGridTime();

    GridTile getActiveTile();

    FeatureCache getFeatureCache();

    void nextTile();

    <T extends Flowable> InputFeed<T> registerInputFeed(UUID memberId, FeedStrategyFactory.FeedType feedType, FeedStrategy<T> feedStrategy);

    <T extends Flowable> InputFeed<T> getInputFeed(FeedStrategyFactory.FeedType type);

    void resolveFeatureReferences();

    TorchieState fork();

    void monitorWith(Eye eye);
}
