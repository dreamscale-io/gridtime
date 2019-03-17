package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Layer {

    private final LayerType layerType;
    private final RelativeSequence relativeSequence;

    private List<MovementEvent> movementsOverTime = new ArrayList<>();


    public Layer(LayerType layerType) {
        this.relativeSequence = new RelativeSequence(1);
        this.layerType = layerType;
    }

    public InnerGeometryClock.Coords addMovement(InnerGeometryClock internalClock, MovementEvent movement) {
        int nextSequence = relativeSequence.increment();

        movement.setCoordinates(internalClock.createCoords(movement.getMoment()));
        movement.setRelativeOffset(nextSequence);

        movementsOverTime.add(movement);

        return movement.getCoordinates();
    }

    public void repairSortingAndSequenceNumbers() {
        movementsOverTime.sort(new Comparator<MovementEvent>() {
            @Override
            public int compare(MovementEvent move1, MovementEvent move2) {

                int compare = move1.getMoment().compareTo(move2.getMoment());

                if (compare == 0) {
                    compare = Integer.compare(move1.getRelativeOffset(), (move2.getRelativeOffset()));
                }

                return compare;
            }
        });

        //fix sequence numbers after resorting
        int sequence = 1;
        for (MovementEvent movement : movementsOverTime) {
            movement.setRelativeOffset(sequence);
            sequence++;
        }
    }


    public LayerType getLayerType() {
        return layerType;
    }

    public RelativeSequence getRelativeSequence() {
        return relativeSequence;
    }

    public void initSequence(int startingValue) {
        relativeSequence.reset(startingValue);
    }
}
