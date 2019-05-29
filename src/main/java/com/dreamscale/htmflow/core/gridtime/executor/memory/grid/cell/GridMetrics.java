package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell;

import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.util.Map;


@NoArgsConstructor
@Getter
@Setter
@ToString
public class GridMetrics  {


    private Map<MetricType, CandleStick> metricsByType = DefaultCollections.map();

    private GridMetrics cascadeToParent;

    public GridMetrics(GridMetrics cascadeToParent) {
        this.cascadeToParent = cascadeToParent;
    }

    private CandleStick findOrCreateCandle(MetricType metricType) {
        CandleStick candle = metricsByType.get(metricType);
        if (candle == null) {
            candle = new CandleStick();
            metricsByType.put(metricType, candle);
        }
        return candle;
    }

    public void addVelocitySample(Duration duration) {
        CandleStick candle = findOrCreateCandle(MetricType.FILE_TRAVERSAL_VELOCITY);
        candle.addSample(duration.getSeconds());
        if (cascadeToParent != null) {
            cascadeToParent.addVelocitySample(duration);
        }
    }

    public void addModificationSample(int modificationCount) {
        CandleStick candle = findOrCreateCandle(MetricType.MODIFICATION_COUNT);
        candle.addSample(modificationCount);
        if (cascadeToParent != null) {
            cascadeToParent.addModificationSample(modificationCount);
        }
    }

    public void addExecutionTimeSample(Duration executionTime) {
        CandleStick candle = findOrCreateCandle(MetricType.EXECUTION_RUN_TIME);

        candle.addSample(executionTime.getSeconds());
        if (cascadeToParent != null) {
            cascadeToParent.addExecutionTimeSample(executionTime);
        }
    }

    public void addExecutionCycleTimeSample(Duration durationBetweenExecution) {
        CandleStick candle = findOrCreateCandle(MetricType.EXECUTION_CYCLE_TIME);
        candle.addSample(durationBetweenExecution.getSeconds());
        if (cascadeToParent != null) {
            cascadeToParent.addExecutionCycleTimeSample(durationBetweenExecution);
        }
    }

    public void addFeelsSample(int feels) {
        CandleStick candle = findOrCreateCandle(MetricType.FEELS);
        candle.addSample(feels);
        if (cascadeToParent != null) {
            cascadeToParent.addFeelsSample(feels);
        }
    }

    public void addWtfSample(boolean isWTF) {
        CandleStick candle = findOrCreateCandle(MetricType.IS_WTF);

        if (isWTF) {
            candle.addSample(1);
        } else {
            candle.addSample(0);
        }
        if (cascadeToParent != null) {
            cascadeToParent.addWtfSample(isWTF);
        }
    }

    public void addLearningSample(boolean isLearning) {
        CandleStick candle = findOrCreateCandle(MetricType.IS_LEARNING);
        if (isLearning) {
            candle.addSample(1);
        } else {
            candle.addSample(0);
        }
        if (cascadeToParent != null) {
            cascadeToParent.addLearningSample(isLearning);
        }
    }

    public void addPairingSample(boolean isPairing) {
        CandleStick candle = findOrCreateCandle(MetricType.IS_PAIRING);

        if (isPairing) {
            candle.addSample(1);
        } else {
            candle.addSample(0);
        }
        if (cascadeToParent != null) {
            cascadeToParent.addPairingSample(isPairing);
        }
    }

    public void addFocusWeightSample(double focusWeight) {
        CandleStick candle = findOrCreateCandle(MetricType.FOCUS_WEIGHT);

        candle.addSample(focusWeight);
        if (cascadeToParent != null) {
            cascadeToParent.addFocusWeightSample(focusWeight);
        }
    }

    @JsonIgnore
    public Duration getTotalTimeInvestment() {
        CandleStick velocityCandle = metricsByType.get(MetricType.FILE_TRAVERSAL_VELOCITY);
        if (velocityCandle != null) {
            return Duration.ofSeconds(Math.round(velocityCandle.getTotal()));
        } else {
            return Duration.ofSeconds(0);
        }
    }

    public void resetMetrics() {
        metricsByType.clear();
    }


    public void combineWith(GridMetrics sourceMetrics) {
        combineAndSet(sourceMetrics, MetricType.FILE_TRAVERSAL_VELOCITY);
        combineAndSet(sourceMetrics, MetricType.MODIFICATION_COUNT);
        combineAndSet(sourceMetrics, MetricType.FEELS);
        combineAndSet(sourceMetrics, MetricType.IS_LEARNING);
        combineAndSet(sourceMetrics, MetricType.IS_WTF);
        combineAndSet(sourceMetrics, MetricType.IS_PAIRING);
        combineAndSet(sourceMetrics, MetricType.EXECUTION_RUN_TIME);
        combineAndSet(sourceMetrics, MetricType.EXECUTION_CYCLE_TIME);
        combineAndSet(sourceMetrics, MetricType.FOCUS_WEIGHT);
    }

    private void combineAndSet(GridMetrics sourceMetrics, MetricType metricType) {
        CandleStick combinedCandle = combineCandles(getMetric(metricType), sourceMetrics.getMetric(metricType));
        if (combinedCandle != null) {
            metricsByType.put(metricType, combinedCandle);
        }
    }

    public CandleStick getMetric(MetricType metricType) {
        return metricsByType.get(metricType);
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


}
