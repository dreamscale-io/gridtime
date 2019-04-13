package com.dreamscale.htmflow.core.feeds.story.feature.metrics;

import java.time.Duration;

public class GridObjectMetrics {

    private CandleStick velocityCandle = new CandleStick();
    private CandleStick modificationCandle = new CandleStick();
    private CandleStick feelsCandle = new CandleStick();
    private CandleStick learningCandle = new CandleStick();
    private CandleStick wtfCandle = new CandleStick();
    private CandleStick pairingCandle = new CandleStick();

    public void addVelocitySample(long durationInSeconds) {
        velocityCandle.addSample(durationInSeconds);
    }

    public void addModificationSample(int modificationCount) {
        modificationCandle.addSample(modificationCount);
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

    public Duration getTotalTimeInvestment() {
        return Duration.ofSeconds(Math.round(velocityCandle.getTotal()));
    }
}
