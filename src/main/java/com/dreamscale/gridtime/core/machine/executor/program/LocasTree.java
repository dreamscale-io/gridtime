package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.Locas;

import java.util.ArrayList;
import java.util.List;

public class LocasTree {


    //each tick is a breatheIn, breathOut, then passing onward to the next... then passing to DB

    //forward, sink?

    //it's not really forwarding... it's groups... like a pivot table.

    //so I've got one thought, that spawns lots of thoughts.

    private List<Locas> locasChain = new ArrayList<>();

    private int currentPosition = -1;

    private Locas lastLocas = null;
    private Locas currentLocas = null;


    void tick() {
        Locas currentLocas = nextLocas();

        //so my first breathe, gets me all the boxes across time,
        // and I really just want to sort those things into groups

        //not have to figure it out up front... like a sorter...

        //currentLocas.runProgram()
        //currentLocas.getOutputGrid()
        //nextLocas.setInputGrid()
    }

    private Locas nextLocas() {
        currentPosition++;

        if (currentPosition < locasChain.size()) {
            lastLocas = currentLocas;
            currentLocas = locasChain.get(currentPosition);
        }

        return currentLocas;
    }

    public void appendLocas(Locas locas) {
        locasChain.add(locas);
    }

}
