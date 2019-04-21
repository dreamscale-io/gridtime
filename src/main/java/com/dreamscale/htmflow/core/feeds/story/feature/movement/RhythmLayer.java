package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class RhythmLayer extends FlowFeature {

    private final RhythmLayerType layerType;
    List<Movement> movements;

    public RhythmLayer(RhythmLayerType layerType) {
        this.layerType = layerType;
        this.movements = new ArrayList<>();
    }

    public List<Movement> getMovements() {
        return movements;
    }

    public void add(Movement movement) {
        movement.initRelativeSequence(this, movements.size() + 1);
        this.movements.add(movement);
    }

    public void repairSortingAndSequenceNumbers() {
        movements.sort(new Comparator<Movement>() {
            @Override
            public int compare(Movement move1, Movement move2) {

                int compare = move1.getMoment().compareTo(move2.getMoment());

                if (compare == 0) {
                    compare = Integer.compare(move1.getRelativeSequence(), (move2.getRelativeSequence()));
                }

                return compare;
            }
        });

        //fix sequence numbers after resorting
        int sequence = 1;
        for (Movement movement : movements) {
            movement.initRelativeSequence(this, sequence);
            sequence++;
        }
    }

    public Movement getLastMovement() {
        Movement lastMovement = null;

        if (movements.size() > 1) {
            lastMovement = movements.get(movements.size() - 1);
        }

        return lastMovement;
    }


}
