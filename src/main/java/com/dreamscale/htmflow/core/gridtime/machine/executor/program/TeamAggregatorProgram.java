package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.GenerateTeamTile;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.AggregateStreamEvent;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.DevNullWire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.Wire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.Locas;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class TeamAggregatorProgram implements Program {

    private final List<Locas> aggregatorChain = DefaultCollections.list();

    private final TorchieState torchieState;
    private final UUID teamId;

    private final UUID workerId;

    private AggregateStreamEvent activeEvent;
    private Metronome.Tick activeTick;

    private  boolean isInitialized;


    public TeamAggregatorProgram(UUID teamId, TorchieState torchieState) {
        this.teamId = teamId;
        this.torchieState = torchieState;

        this.isInitialized = false;

        this.workerId = UUID.randomUUID();
    }

    public void tick(Wire inputStreamEventWire) {

        inputStreamEventWire.markDone(workerId);

        if (inputStreamEventWire.hasNext()) {
            activeEvent = inputStreamEventWire.pullNext(workerId);
            activeTick = Metronome.createTick(activeEvent.getGridTime());
        }

    }
    public Metronome.Tick getActiveTick() {
        return activeTick;
    }

    @Override
    public List<TileInstructions> getInstructionsAtActiveTick() {
        List<TileInstructions> instructions = new ArrayList<>();

        instructions.add(generateAggregateTickInstructions(activeTick));

        //so I've got an aggregator tile event thing...

        //each aggregate is for a particular grid time, and has an aggregate containing the event to process
        //so if I've got 10 team members processing their feeds, I'll get an event for each of the team members,

        //then I might process a loop, such that each team member's tile, I get 8/12 team events, cool, I process those
        //then I get the other 4 team members, process the tile again and update it with 12/12

        //and what I do with each iteration?  For a partial read for a specific tile?

        //inputs, are all the input tiles for the team members for that particular tile.
        //inputs, are the old version of the tile, so I can get the Id, and update it, actually, I don't really need this until we save?

        //I've got % wtf, % learn, and a weight for each for each person.

        //Then my cumulative %wtf, takes the relative weight of all the weights of all the people.

        //so each person is a column? AJ initials on the column.


        //if I take the old aggregate, and the old rows

        //remake the spreadsheet, everytime...?  Or the rows, that are included already...

        return instructions;
    }

    @Override
    public boolean isDone() {
        return false;
    }


    private TileInstructions generateAggregateTickInstructions(Metronome.Tick aggregateTick) {

        return new GenerateTeamTile(torchieState, aggregatorChain, aggregateTick);
    }

    public void addAggregator( Locas locas) {
        aggregatorChain.add(locas );
    }


}
