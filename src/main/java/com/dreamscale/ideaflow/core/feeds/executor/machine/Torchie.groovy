package com.dreamscale.ideaflow.core.feeds.executor.machine

class Torchie {

    boolean isAlive;


    void die() {
        isAlive = false;
    }

    /**
     * The main game loop of your Torchie!
     */
    void flow() {

        if (!isAlive) {
            isAlive = true;
        }





    }

    /**
     * Push the ShapeFinder to the client
     * @param shapeFinder
     */
    IdeaFlowNetwork pullWhatMatters(ShapeFinder shapeFinder ) {

    }

    ShapeFinder resolveTension(IdeaFlowNetwork ideaFlowNetwork) {
        null
    }

    IdeaFlowNetwork loadSavedFlowContext() {


    }

    ShapeFinder loadSavedSearchContext() {


    }

    /**
     * This is the main game loop
     */
    private class Life implements Runnable {

        private Metronome metronome

        Life() {
            this.metronome = new Metronome();
        }

        @Override
        void run() {

            ShapeFinder activeSearch = loadSavedSearchContext()
            IdeaFlowNetwork activeFlows = loadActiveFlowContext()

            while (isAlive) {
                metronome.tick();

                activeFlows = pullWhatMatters(activeSearch)
                activeSearch = resolveTension(activeFlows)
            }
        }

    }
}
