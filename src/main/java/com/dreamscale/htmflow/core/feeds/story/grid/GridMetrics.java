package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.domain.tile.CandleType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@NoArgsConstructor
@Getter
@Setter
@ToString
public class GridMetrics  {

    private CandleStick velocityCandle;
    private CandleStick modificationCandle;
    private CandleStick feelsCandle;
    private CandleStick learningCandle;
    private CandleStick wtfCandle;
    private CandleStick pairingCandle;
    private CandleStick executionCandle;
    private CandleStick executionCycleTimeCandle;
    private CandleStick focusWeightCandle;

    private GridMetrics cascadeToParent;

    public GridMetrics(GridMetrics cascadeToParent) {
        this.cascadeToParent = cascadeToParent;
    }


    public void addVelocitySample(Duration duration) {
        if (velocityCandle == null) {
            velocityCandle = new CandleStick();
        }
        velocityCandle.addSample(duration.getSeconds());
        if (cascadeToParent != null) {
            cascadeToParent.addVelocitySample(duration);
        }
    }

    public void addModificationSample(int modificationCount) {
        if (modificationCandle == null) {
            modificationCandle = new CandleStick();
        }
        modificationCandle.addSample(modificationCount);
        if (cascadeToParent != null) {
            cascadeToParent.addModificationSample(modificationCount);
        }
    }

    public void addExecutionSample(Duration executionTime) {
        if (executionCandle == null) {
            executionCandle = new CandleStick();
        }
        executionCandle.addSample(executionTime.getSeconds());
        if (cascadeToParent != null) {
            cascadeToParent.addExecutionSample(executionTime);
        }
    }

    public void addExecutionCycleTimeSample(Duration durationBetweenExecution) {
        if (executionCycleTimeCandle == null) {
            executionCycleTimeCandle = new CandleStick();
        }

        executionCycleTimeCandle.addSample(durationBetweenExecution.getSeconds());
        if (cascadeToParent != null) {
            cascadeToParent.addExecutionCycleTimeSample(durationBetweenExecution);
        }
    }

    public void addFeelsSample(int feels) {
        if (feelsCandle == null) {
            feelsCandle = new CandleStick();
        }
        feelsCandle.addSample(feels);
        if (cascadeToParent != null) {
            cascadeToParent.addFeelsSample(feels);
        }
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
        if (cascadeToParent != null) {
            cascadeToParent.addWtfSample(isWTF);
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
        if (cascadeToParent != null) {
            cascadeToParent.addLearningSample(isLearning);
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
        if (cascadeToParent != null) {
            cascadeToParent.addPairingSample(isPairing);
        }
    }

    public void addFocusWeightSample(double focusWeight) {
        if (focusWeightCandle == null) {
            focusWeightCandle = new CandleStick();
        }
        focusWeightCandle.addSample(focusWeight);
        if (cascadeToParent != null) {
            cascadeToParent.addFocusWeightSample(focusWeight);
        }
    }

    @JsonIgnore
    public Duration getTotalTimeInvestment() {
        if (velocityCandle != null) {
            return Duration.ofSeconds(Math.round(velocityCandle.getTotal()));
        } else {
            return Duration.ofSeconds(0);
        }
    }

    public void resetMetrics() {
        velocityCandle = null;
        modificationCandle = null;
        feelsCandle = null;
        learningCandle = null;
        wtfCandle = null;
        pairingCandle = null;
        executionCandle = null;
        focusWeightCandle = null;
        executionCycleTimeCandle = null;
    }


    public void combineWith(GridMetrics sourceMetrics) {
        velocityCandle = combineCandles(velocityCandle, sourceMetrics.velocityCandle);
        modificationCandle = combineCandles(modificationCandle, sourceMetrics.modificationCandle);
        feelsCandle = combineCandles(feelsCandle, sourceMetrics.feelsCandle);
        learningCandle = combineCandles(learningCandle, sourceMetrics.learningCandle);
        wtfCandle = combineCandles(wtfCandle, sourceMetrics.wtfCandle);
        pairingCandle = combineCandles(pairingCandle, sourceMetrics.pairingCandle);
        executionCandle = combineCandles(executionCandle, sourceMetrics.executionCandle);
        executionCycleTimeCandle = combineCandles(executionCycleTimeCandle, sourceMetrics.executionCycleTimeCandle);
        focusWeightCandle = combineCandles(focusWeightCandle, sourceMetrics.focusWeightCandle);
    }

    private CandleStick combineCandles(CandleStick destCandle, CandleStick candleToCombine) {
        if (destCandle == null && candleToCombine != null) {
            destCandle = new CandleStick();
        }
        if (candleToCombine != null) {
            destCandle.combineAggregate(candleToCombine);
        }

        return destCandle;
    }


    public Map<CandleType, CandleStick> getCandleMap() {
        Map<CandleType, CandleStick> candleMap = new HashMap<>();
        if (velocityCandle != null) candleMap.put(CandleType.VELOCITY, velocityCandle);
        if (modificationCandle != null) candleMap.put(CandleType.MODIFICATION, modificationCandle);
        if (feelsCandle != null) candleMap.put(CandleType.FEELS, feelsCandle);
        if (learningCandle != null) candleMap.put(CandleType.LEARNING, learningCandle);
        if (wtfCandle != null) candleMap.put(CandleType.WTF, wtfCandle);
        if (pairingCandle != null) candleMap.put(CandleType.PAIRING, pairingCandle);
        if (executionCandle != null) candleMap.put(CandleType.EXECUTIONS, executionCandle);
        if (executionCycleTimeCandle != null) candleMap.put(CandleType.EXECUTION_CYCLE_TIME, executionCycleTimeCandle);
        if (focusWeightCandle != null) candleMap.put(CandleType.FOCUS_WEIGHT, focusWeightCandle);

        return candleMap;
    }
}
