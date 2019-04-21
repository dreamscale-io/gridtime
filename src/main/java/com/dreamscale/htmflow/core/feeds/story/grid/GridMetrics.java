package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;

@Getter
@ToString
public class GridMetrics extends FlowFeature {

    private CandleStick velocityCandle = new CandleStick();
    private CandleStick modificationCandle = new CandleStick();
    private CandleStick feelsCandle = new CandleStick();
    private CandleStick learningCandle = new CandleStick();
    private CandleStick wtfCandle = new CandleStick();
    private CandleStick pairingCandle = new CandleStick();
    private CandleStick executionCandle = new CandleStick();
    private CandleStick executionCycleTimeCandle = new CandleStick();
    private CandleStick focusWeightCandle = new CandleStick();


    public void addVelocitySample(Duration duration) {
        velocityCandle.addSample(duration.getSeconds());
    }

    public void addModificationSample(int modificationCount) {
        modificationCandle.addSample(modificationCount);
    }

    public void addExecutionSample(Duration executionTime) {
        executionCandle.addSample(executionTime.getSeconds());
    }

    public void addExecutionCycleTimeSample(Duration durationBetweenExecution) {
        executionCycleTimeCandle.addSample(durationBetweenExecution.getSeconds());
    }

    public void addFeelsSample(int feels) {
        feelsCandle.addSample(feels);
    }

    public void addWtfSample(boolean isWTF) {
        if (isWTF) {
            wtfCandle.addSample(1);
        } else {
            wtfCandle.addSample(0);
        }
    }

    public void addLearningSample(boolean isLearning) {
        if (isLearning) {
            learningCandle.addSample(1);
        } else {
            learningCandle.addSample(0);
        }
    }

    public void addPairingSample(boolean isPairing) {
        if (isPairing) {
            pairingCandle.addSample(1);
        } else {
            pairingCandle.addSample(0);
        }
    }

    public void addFocusWeightSample(double focusWeight) {
        focusWeightCandle.addSample(focusWeight);
    }

    public Duration getTotalTimeInvestment() {
        return Duration.ofSeconds(Math.round(velocityCandle.getTotal()));
    }

    public void resetMetrics() {
        velocityCandle = new CandleStick();
        modificationCandle = new CandleStick();
        feelsCandle = new CandleStick();
        learningCandle = new CandleStick();
        wtfCandle = new CandleStick();
        pairingCandle = new CandleStick();
        executionCandle = new CandleStick();
        focusWeightCandle = new CandleStick();
        executionCycleTimeCandle = new CandleStick();

    }

    public void nullOutEmptyMetrics() {
        if (velocityCandle.getSampleCount() == 0) {
            velocityCandle = null;
        }
        if (modificationCandle.getSampleCount() == 0) {
            modificationCandle = null;
        }
        if (feelsCandle.getSampleCount() == 0) {
            feelsCandle = null;
        }
        if (learningCandle.getSampleCount() == 0) {
            learningCandle = null;
        }
        if (wtfCandle.getSampleCount() == 0) {
            wtfCandle = null;
        }
        if (pairingCandle.getSampleCount() == 0) {
            pairingCandle = null;
        }
        if (executionCandle.getSampleCount() == 0) {
            executionCandle = null;
        }
        if (focusWeightCandle.getSampleCount() == 0) {
            focusWeightCandle = null;
        }

        if (executionCycleTimeCandle.getSampleCount() == 0) {
            executionCycleTimeCandle = null;
        }
    }

    public void combineWith(GridMetrics sourceMetrics) {
        velocityCandle.combineAggregate(sourceMetrics.velocityCandle);
        modificationCandle.combineAggregate(sourceMetrics.modificationCandle);
        feelsCandle.combineAggregate(sourceMetrics.feelsCandle);
        learningCandle.combineAggregate(sourceMetrics.learningCandle);
        wtfCandle.combineAggregate(sourceMetrics.wtfCandle);
        pairingCandle.combineAggregate(sourceMetrics.pairingCandle);
        executionCandle.combineAggregate(sourceMetrics.executionCandle);
        executionCycleTimeCandle.combineAggregate(sourceMetrics.executionCycleTimeCandle);
        focusWeightCandle.combineAggregate(sourceMetrics.focusWeightCandle);
    }


}
