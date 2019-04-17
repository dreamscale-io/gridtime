package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.Duration;

public class GridMetrics extends FlowFeature {

    private CandleStick velocityCandle;
    private CandleStick modificationCandle;
    private CandleStick feelsCandle;
    private CandleStick learningCandle;
    private CandleStick wtfCandle;
    private CandleStick pairingCandle;
    private CandleStick executionCandle;

    public void addVelocitySample(Duration duration) {
        if (velocityCandle == null) {
            velocityCandle = new CandleStick();
        }
        velocityCandle.addSample(duration.getSeconds());
    }

    public void addModificationSample(int modificationCount) {
        if (modificationCandle == null) {
            modificationCandle = new CandleStick();
        }
        modificationCandle.addSample(modificationCount);
    }

    public void addExecutionSample(Duration executionTime) {
        if (executionCandle == null) {
            executionCandle = new CandleStick();
        }
        executionCandle.addSample(executionTime.getSeconds());
    }

    public void addFeelsSample(int feels) {
        if (feelsCandle == null) {
            feelsCandle = new CandleStick();
        }
        feelsCandle.addSample(feels);
    }

    public void addWtfSample(boolean isWTF) {
        if (wtfCandle == null) {
            wtfCandle = new CandleStick();
        }
        if (isWTF) {
            wtfCandle.addSample(1);
        } else {
            wtfCandle.addSample(0);
        }
    }

    public void addLearningSample(boolean isLearning) {
        if (learningCandle == null) {
            learningCandle = new CandleStick();
        }
        if (isLearning) {
            learningCandle.addSample(1);
        } else {
            learningCandle.addSample(0);
        }
    }

    public void addPairingSample(boolean isPairing) {
        if (pairingCandle == null) {
            pairingCandle = new CandleStick();
        }
        if (isPairing) {
            pairingCandle.addSample(1);
        } else {
            pairingCandle.addSample(0);
        }
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
    }

    public void combineWith(GridMetrics sourceMetrics) {
        velocityCandle.combineAggregate(sourceMetrics.velocityCandle);
        modificationCandle.combineAggregate(sourceMetrics.modificationCandle);
        feelsCandle.combineAggregate(sourceMetrics.feelsCandle);
        learningCandle.combineAggregate(sourceMetrics.learningCandle);
        wtfCandle.combineAggregate(sourceMetrics.wtfCandle);
        pairingCandle.combineAggregate(sourceMetrics.pairingCandle);
        executionCandle.combineAggregate(sourceMetrics.executionCandle);
    }
}
