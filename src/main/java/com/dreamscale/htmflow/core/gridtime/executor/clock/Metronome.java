package com.dreamscale.htmflow.core.gridtime.executor.clock;

import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.Flow;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.GenerateAggregateUpTile;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.GenerateNextTile;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Metronome {

    private GeometryClock clock;

    private GeometryClock.GridTime fromClockPosition;
    private GeometryClock.GridTime toClockPosition;

    private final FeaturePool featurePool;
    private final List<Flow> pullChain;

    private boolean isHalted = false;

    public Metronome(FeaturePool featurePool, LocalDateTime startTime) {
        this.featurePool = featurePool;

        this.clock = new GeometryClock(startTime);

        this.fromClockPosition = clock.getActiveGridTime();
        this.toClockPosition = clock.getActiveGridTime();

        this.pullChain = new ArrayList<>();

        featurePool.gotoGridTile(fromClockPosition);
    }

    public GeometryClock.GridTime getActiveCoordinates() {
        return fromClockPosition;
    }

    public List<TileInstructions> tick() {
        if (isHalted) {
            return DefaultCollections.emptyList();
        }

        GeometryClock.GridTime nextCoordinates = clock.next();

        this.fromClockPosition = this.toClockPosition;
        this.toClockPosition = nextCoordinates;

        List<TileInstructions> instructions = new ArrayList<>();

        instructions.add(createInstructionsForFlowTick());

        if (!Objects.equals(fromClockPosition.getDayPart(), toClockPosition.getDayPart())) {
            instructions.add(createTileInstructionsForAggregateTick(ZoomLevel.DAY_PART));
        }

        if (!Objects.equals(fromClockPosition.getDay(), toClockPosition.getDay())) {
            instructions.add(createTileInstructionsForAggregateTick(ZoomLevel.DAY));
        }

        if (!Objects.equals(fromClockPosition.getBlockWeek(), toClockPosition.getBlockWeek())) {
            instructions.add(createTileInstructionsForAggregateTick(ZoomLevel.WEEK));
        }

        if (!Objects.equals(fromClockPosition.getBlock(), toClockPosition.getBlock())) {
            instructions.add(createTileInstructionsForAggregateTick(ZoomLevel.BLOCK));
        }
        if (!Objects.equals(fromClockPosition.getYear(), toClockPosition.getYear())) {
            instructions.add(createTileInstructionsForAggregateTick(ZoomLevel.YEAR));
        }
        return instructions;
    }

    private TileInstructions createInstructionsForFlowTick() {
        return new GenerateNextTile(featurePool, pullChain, fromClockPosition, toClockPosition);
    }

    private TileInstructions createTileInstructionsForAggregateTick(ZoomLevel zoomLevel) {
        return new GenerateAggregateUpTile(featurePool, zoomLevel, fromClockPosition.getClockTime());
    }

    public boolean canTick() {
        if (isHalted) {
            return false;
        }
        return clock.getNextTickTime().isBefore(LocalDateTime.now());
    }


    public GeometryClock.GridTime getFromClockPosition() {
        return this.fromClockPosition;
    }

    public GeometryClock.GridTime getToClockPosition() {
        return this.toClockPosition;
    }

    public void addFlowToPullChain(Flow flow) {
        this.pullChain.add(flow);
    }


    public String getGridTime() {
        return getActiveCoordinates().getFormattedGridTime();
    }

    public void halt() {
        isHalted = true;
    }

    public void resume() {
        isHalted = false;
    }
}
