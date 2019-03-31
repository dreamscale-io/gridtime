package com.dreamscale.ideaflow.core.feeds.story.feature.sequence;

import com.dreamscale.ideaflow.core.feeds.clock.InnerGeometryClock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class IdeaFlowLayer {

    private final IdeaFlowLayerType layerType;
    private final RelativeSequence relativeSequence;

    private List<IdeaFlowMovementEvent> movementsOverTime = new ArrayList<>();


    public IdeaFlowLayer(IdeaFlowLayerType layerType) {
        this.relativeSequence = new RelativeSequence(1);
        this.layerType = layerType;
    }

    public InnerGeometryClock.Coords addMovement(InnerGeometryClock internalClock, IdeaFlowMovementEvent movement) {
        int nextSequence = relativeSequence.increment();

        movement.setCoordinates(internalClock.createCoords(movement.getMoment()));
        movement.setRelativeOffset(nextSequence);

        movementsOverTime.add(movement);

        return movement.getCoordinates();
    }

    public void repairSortingAndSequenceNumbers() {
        movementsOverTime.sort(new Comparator<IdeaFlowMovementEvent>() {
            @Override
            public int compare(IdeaFlowMovementEvent move1, IdeaFlowMovementEvent move2) {

                int compare = move1.getMoment().compareTo(move2.getMoment());

                if (compare == 0) {
                    compare = Integer.compare(move1.getRelativeOffset(), (move2.getRelativeOffset()));
                }

                return compare;
            }
        });

        //fix sequence numbers after resorting
        int sequence = 1;
        for (IdeaFlowMovementEvent movement : movementsOverTime) {
            movement.setRelativeOffset(sequence);
            sequence++;
        }
    }


    public IdeaFlowLayerType getLayerType() {
        return layerType;
    }

    public RelativeSequence getRelativeSequence() {
        return relativeSequence;
    }

    public void initSequence(int startingValue) {
        relativeSequence.reset(startingValue);
    }

    public List<IdeaFlowMovementEvent> getMovements() {
        return movementsOverTime;
    }
}
