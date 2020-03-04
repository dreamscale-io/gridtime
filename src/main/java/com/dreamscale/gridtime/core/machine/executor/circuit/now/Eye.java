package com.dreamscale.gridtime.core.machine.executor.circuit.now;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyDoneTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.AlarmScript;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.TimeBomb;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.Locas;
import com.dreamscale.gridtime.core.machine.executor.worker.WhatsNextWheel;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import lombok.AllArgsConstructor;

import java.util.List;

public class Eye {

    WhatsNextWheel<Locas> locasJobQueue;

    private List<Scope> focusInScopes = DefaultCollections.list();
    private List<MetricWatcher> metricWatchers = DefaultCollections.list();
    private List<FeatureReference> watchForFeatures = DefaultCollections.list();
    private List<FeatureReference> watchForFeaturesMissing = DefaultCollections.list();

    private List<FeatureReference> wtfMarkers = DefaultCollections.list();
    private List<NotifyDoneTrigger> notifyTriggers = DefaultCollections.list();

    private double watchForPainAboveThreshold = -3;
    private double watchForJoyAboveThreshold = 3;



    //locas is a Context predictive thread...

    public Eye focusInScope(Scope scope) {
        this.focusInScopes.add(scope);
        return this;
    }

    public Eye watchFor(FeatureReference feature) {
        this.watchForFeatures.add(feature);
        return this;
    }

    public Eye watchForFeatures(List<FeatureReference> featureTags) {
        this.watchForFeatures.addAll(featureTags);
        return this;
    }

    public Eye watchForFeaturesMissing(List<FeatureReference> featureTags) {
        this.watchForFeaturesMissing.addAll(featureTags);
        return this;

    }

    public Eye watchForPainAboveThreshold(double threshold) {
        this.metricWatchers.add(new MetricBelowWatcher(MetricRowKey.ZOOM_AVG_FLAME, threshold));
        return this;
    }

    public Eye watchForJoyAboveThreshold(double threshold) {
        this.metricWatchers.add(new MetricAboveWatcher(MetricRowKey.ZOOM_AVG_FLAME, threshold));
        return this;
    }

    public Eye watchForMetricAboveThreshold(MetricRowKey metricKey, double threshold) {
        this.metricWatchers.add(new MetricAboveWatcher(metricKey, threshold));
        return this;
    }

    public Eye watchForMetricBelowThreshold(MetricRowKey metricKey, double threshold) {
        this.metricWatchers.add(new MetricBelowWatcher(metricKey, threshold));
        return this;
    }

    public void pushIntoFocus(IMusicGrid musicGrid) {
        //this is the last matrix of information that we processed...
        //LRU cache the most recent thingies
    }

    public TwilightMap see() {
        //look at all the music grids, for any of the things, and provide a set of FeatureTags, with Feels ratings,
        // then arrange them on a 2x2 grid, where similarity of type of thing affects clustering.

        //regenerate over time, keep common feature cached... LRU pings the stuff in the Cache and updates the stats

        //play these back as a generated synth music note... with "gravity" to a beat.
        //then power to bubble up interesting
        return new TwilightMap();
    }

    public void markWTF(FeatureReference featureReference) {
        //will create a watch loop, that constantly looks for activity related to a certain feature,
        // on a keep alive sampling program
        // generating instructions, activity, monitoring suspicion on the fly, and creating 2 out of 3 sorts of monitors.
        // increasing sensitivity

        this.wtfMarkers.add(featureReference);
    }

    public void onThresholdTrigger(NotifyDoneTrigger notifyTrigger) {
        this.notifyTriggers.add(notifyTrigger);
    }

    public Locas bubbleUp(TwilightMap searchForSimilarMap) {
        //navigate in a few layers based on provided search criteria, and aggregate a metrics grid,
        //this is a job that requires exec cycle work, but is dynamically generated based on the current map

        //just do next round of connections, following links for one cycle of DB processing stuff.
        return null;
    }

    public Locas searchParetoFocus() {
        //here's a program that runs in a context, based on a search of the latest TwilightMap.

        //we will do some simple thinking.  Generate thoughts, and put them on the thought wheel.
        //return the thought to run based on self-inspection...
        return null;
    }


    public List<TimeBomb> generateTimeBombMonitors() {
        return DefaultCollections.list();
    }

    public List<AlarmScript> triggerAlarms() {
        return DefaultCollections.list();
    }

    private interface MetricWatcher {

    }

    @AllArgsConstructor
    private class MetricBelowWatcher implements MetricWatcher {
        MetricRowKey metricRowKey;
        double threshold;
    }

    @AllArgsConstructor
    private class MetricAboveWatcher implements MetricWatcher {
        MetricRowKey metricRowKey;
        double threshold;
    }

}



