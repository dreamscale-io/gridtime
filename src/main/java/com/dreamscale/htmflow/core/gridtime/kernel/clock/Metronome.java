package com.dreamscale.htmflow.core.gridtime.kernel.clock;

import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.MetronomeProgram;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions.TileInstructions;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Metronome {

    private GeometryClock clock;

    private GeometryClock.GridTime fromGridTime;
    private GeometryClock.GridTime toGridTime;

    private final MetronomeProgram metronomeJob;

    private boolean isHalted = false;

    public Metronome(MetronomeProgram metronomeJob) {
        this.metronomeJob = metronomeJob;

        this.clock = new GeometryClock(metronomeJob.getStartPosition());

        this.fromGridTime = clock.getActiveGridTime();
        this.toGridTime = clock.next();

        metronomeJob.gotoPosition(fromGridTime);
    }

    public GeometryClock.GridTime getActiveCoordinates() {
        return fromGridTime;
    }

    public List<TileInstructions> tick() {
        if (isHalted) {
            return DefaultCollections.emptyList();
        }

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

        GeometryClock.GridTime nextCoordinates = clock.next();

        this.fromGridTime = this.toGridTime;
        this.toGridTime = nextCoordinates;

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
        GeometryClock.GridTime toZoomedOutCoords = toGridTime.toZoomLevel(zoomLevel);
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

    public void halt() {
        isHalted = true;
    }

    public void resume() {
        isHalted = false;
    }

    public String getTickPosition() {
        return getActiveCoordinates().getFormattedGridTime();
    }
}
