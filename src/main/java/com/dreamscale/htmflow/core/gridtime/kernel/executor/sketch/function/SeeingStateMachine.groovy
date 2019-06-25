package com.dreamscale.htmflow.core.gridtime.kernel.executor.sketch.function

import com.dreamscale.htmflow.core.gridtime.kernel.executor.sketch.window.FlowWindow
import com.dreamscale.htmflow.core.gridtime.kernel.executor.sketch.state.FlowOneWithResonance

class SeeingStateMachine {

    ProgramArrow activeArrow;
    IdeaFlowNetwork activeState;

    SeeingStateMachine() {
        this.activeArrow = new FlowOneWithResonance();
    }

    ProgramArrow next(FlowWindow flowWindow, ProgramArrow programArrow) {

        //so for the first part of my life, I just listen... and flow with resonance in response to listening
        //this is all I do as a baby, is listen in flow... it is the beginning of the conscious programming
        //opening eyes...
        //sensing the world...
        //listening and feeling and flowing with resonance

        //Torchie as a baby, just listens and flows with you...
        //does what you do... tries to predict and mimic the resonance
        //game is predict and flow with...

        //I don't start having tension until I feel like I know better... until I feel like you're going the wrong way
        //tension rises, tension throws exceptions, over and over again,
        // exception throwing on thresholds of going the wrong way

        //I can't get here until I think I'm right and you're wrong, until certainty happens in system 1.
        //system 1 is homing toward certainty
        //system 2 is tension toward winning, on demand response to tension, starts to do pressure/release
        //system 3 is balance... which is integrating 1,2... choosing love and death, fragmentation, integration, water

        //so lets start there... as we listen to the engineer code
        //I've got a flow feed coming into the system, providing a Flow context, that's a continuously sliding window.

        //what do I.see() in the window?
        //where am I in the context of the game?

        //window can be aggregated across different time scales

        //here's the situation, identify what matters, push/pull danger signals,
        // and if we're going to transition to a new state, based on the emergent context

        //so if I see danger shapes, I might flip to a different magnet
        //if I see an object like me, where I predicting joining will result in Thrive

        //so the monitor, should be doing threat detection all the time
        //determining if we should be pushing away at any given moment
        //the stronger the push signal, we trip a threshold, and fire a rule

        //lets start with just implementing a Seeing machine

        this.activeArrow = programArrow;
    }

    IdeaFlowNetwork see() {
        //this returns the internal context object of all the linked flow chains, over some time window

        return null;
    }

}
