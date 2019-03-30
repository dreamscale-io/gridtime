package com.dreamscale.htmflow.core.feeds.executor.machine.game

import com.dreamscale.htmflow.core.feeds.executor.machine.IdeaFlowNetwork

class HomingGameMachine {

    HomingArrow activeArrow
    IdeaFlowNetwork gameState;

    //this thing operates on top of seeing, and makes decisions and flows, as opposed to just watches
    //dependent on SeeingStateMachine, but animates the body, and chooses movements

    //this is the meta system that decides when to switch ProgramArrows

    //meta program makes decisions about what is the game, and evaluates whether we're winning the game

    //if we've got several games going at once, each one gives us a slider upon evaluation.

    HomingArrow next(GameContext gameContext, HomingArrow homingArrow) {
        return homingArrow;
    }

    SliderValue amIWinning() {
        //evaluates whether we're winning or losing, on a scale from -5, to +5, red/purple flames
        //this gets used in determine whether focusing energy on this, to "think" about strategy is worth it
        return new SliderValue(5);
    }

    GamePlaySequence generateStrategy() {
        //evaluates the game situation, and predicts a potential path to success,
        //determines the best possible strategy to win the game, and returns as a strategy
        // as a sequence of arrows and expectations, like... how is the story supposed to go

        //In actuality, this strategy might be a composite of alternative stories in a fabric
        //such that this game context can be woven into other game contexts where there are similar shapes
    }


}
