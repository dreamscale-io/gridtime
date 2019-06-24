package com.dreamscale.htmflow.core.gridtime.machine.clock;

import com.dreamscale.htmflow.core.gridtime.machine.executor.job.MetronomeJob;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.TileInstructions;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Metronome {

    private GeometryClock clock;

    private GeometryClock.GridTime fromGridTime;
    private GeometryClock.GridTime toGridTime;

    private final MetronomeJob metronomeJob;

    private boolean isHalted = false;

    public Metronome(MetronomeJob metronomeJob) {
        this.metronomeJob = metronomeJob;

        this.clock = new GeometryClock(metronomeJob.getStartPosition());

        this.fromGridTime = clock.getActiveGridTime();
        this.toGridTime = clock.getActiveGridTime();

        metronomeJob.gotoPosition(fromGridTime);
    }

    public GeometryClock.GridTime getActiveCoordinates() {
        return fromGridTime;
    }

    public List<TileInstructions> tick() {
        if (isHalted) {
            return DefaultCollections.emptyList();
        }

        GeometryClock.GridTime nextCoordinates = clock.next();

        this.fromGridTime = this.toGridTime;
        this.toGridTime = nextCoordinates;

        List<TileInstructions> instructions = new ArrayList<>();

        addIfNotNull(instructions, getJobInstructionsForFlowTick());

        if (!Objects.equals(fromGridTime.getDayPart(), toGridTime.getDayPart())) {
            addIfNotNull(instructions, getJobInstructionsForAggregateTick(ZoomLevel.DAY_PART));
        }

        if (!Objects.equals(fromGridTime.getDay(), toGridTime.getDay())) {
            addIfNotNull(instructions, getJobInstructionsForAggregateTick(ZoomLevel.DAY));
        }

        if (!Objects.equals(fromGridTime.getBlockWeek(), toGridTime.getBlockWeek())) {
            addIfNotNull(instructions, getJobInstructionsForAggregateTick(ZoomLevel.WEEK));
        }

        if (!Objects.equals(fromGridTime.getBlock(), toGridTime.getBlock())) {
            addIfNotNull(instructions, getJobInstructionsForAggregateTick(ZoomLevel.BLOCK));
        }
        if (!Objects.equals(fromGridTime.getYear(), toGridTime.getYear())) {
            addIfNotNull(instructions, getJobInstructionsForAggregateTick(ZoomLevel.YEAR));
        }
        return instructions;
    }

    private void addIfNotNull(List<TileInstructions> instructions, TileInstructions newInstruction) {
        if (newInstruction != null) {
            instructions.add(newInstruction);
        }
    }

    private TileInstructions getJobInstructionsForFlowTick() {
        return metronomeJob.baseTick(fromGridTime, toGridTime);
    }

    private TileInstructions getJobInstructionsForAggregateTick(ZoomLevel zoomLevel) {
        GeometryClock.GridTime toZoomedOutCoords = fromGridTime.toZoomLevel(zoomLevel);
        GeometryClock.GridTime fromZoomedOutCoords = toZoomedOutCoords.panLeft();

        return metronomeJob.aggregateTick(fromZoomedOutCoords, toZoomedOutCoords);
    }

    public boolean canTick() {
        if (isHalted) {
            return false;
        }
        return metronomeJob.canTick(clock.getNextTickTime());
    }


    public GeometryClock.GridTime getFromGridTime() {
        return this.fromGridTime;
    }

    public GeometryClock.GridTime getToGridTime() {
        return this.toGridTime;
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
