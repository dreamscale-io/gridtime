package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RhythmLayer {

    private final RhythmLayerType layerType;
    private final RelativeSequence relativeSequence;

    private Movement carryOverLastMovement;
    private List<Movement> movementsOverTime = new ArrayList<>();


    public RhythmLayer(RhythmLayerType layerType) {
        this.relativeSequence = new RelativeSequence(1);
        this.layerType = layerType;
    }

    public InnerGeometryClock.Coords addMovement(InnerGeometryClock internalClock, Movement movement) {
        int nextSequence = relativeSequence.increment();

        movement.setCoordinates(internalClock.createCoords(movement.getMoment()));
        movement.setRelativeOffset(nextSequence);

        movementsOverTime.add(movement);

        return movement.getCoordinates();
    }

    public void repairSortingAndSequenceNumbers() {
        movementsOverTime.sort(new Comparator<Movement>() {
            @Override
            public int compare(Movement move1, Movement move2) {

                int compare = move1.getMoment().compareTo(move2.getMoment());

                if (compare == 0) {
                    compare = Integer.compare(move1.getRelativeOffset(), (move2.getRelativeOffset()));
                }

                return compare;
            }
        });

        //fix sequence numbers after resorting
        int sequence = 1;
        for (Movement movement : movementsOverTime) {
            movement.setRelativeOffset(sequence);
            sequence++;
        }
    }

    public RhythmLayerType getLayerType() {
        return layerType;
    }

    public List<Movement> getMovements() {
        return movementsOverTime;
    }

    public Movement getLastMovement() {
        Movement lastMovement = null;

        if (movementsOverTime.size() > 1) {
            lastMovement = movementsOverTime.get(movementsOverTime.size() - 1);
        }

        if (lastMovement == null) {
            lastMovement = carryOverLastMovement;
        }

        return lastMovement;
    }

    public void initContext(Movement lastMovement) {
        this.carryOverLastMovement = lastMovement;
    }
}
