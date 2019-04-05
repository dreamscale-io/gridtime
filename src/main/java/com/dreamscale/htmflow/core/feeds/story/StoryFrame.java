package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.band.*;
import com.dreamscale.htmflow.core.feeds.story.feature.context.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.BoxAndBridgeStructure;
import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInPlace;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.FocusPlace;
import com.dreamscale.htmflow.core.feeds.story.mapper.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class StoryFrame {

    private final OuterGeometryClock.Coords storyFrameCoordinates;
    private final ZoomLevel zoomLevel;

    private final InnerGeometryClock internalClock;

    private final FlowContextMapper contextMapper;
    private final SpatialGeometryMapper spatialGeometryMapper;
    private final FlowRhythmMapper flowRhythmMapper;
    private final FlowBandMapper timeBandMapper;


    public StoryFrame(OuterGeometryClock.Coords storyFrameCoordinates, ZoomLevel zoomLevel) {
        this.storyFrameCoordinates = storyFrameCoordinates;
        this.zoomLevel = zoomLevel;

        this.internalClock = new InnerGeometryClock(
                storyFrameCoordinates.getClockTime(),
                storyFrameCoordinates.panRight(zoomLevel).getClockTime());

        this.contextMapper = new FlowContextMapper(internalClock.getFromClockTime(), internalClock.getToClockTime());
        this.spatialGeometryMapper = new SpatialGeometryMapper(internalClock.getFromClockTime(), internalClock.getToClockTime());
        this.flowRhythmMapper = new FlowRhythmMapper(internalClock.getFromClockTime(), internalClock.getToClockTime());
        this.timeBandMapper = new FlowBandMapper(internalClock.getFromClockTime(), internalClock.getToClockTime());

    }

    /**
     * Change the active context, such as starting project, task, or intention
     */

    public void beginContext(ContextBeginningEvent contextBeginningEvent) {
        Movement movement = contextMapper.beginContext(contextBeginningEvent);
        flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, movement);
    }

    /**
     * End an active context, such as project, task, or intention
     */

    public void endContext(ContextEndingEvent contextEndingEvent) {
        Movement movement = contextMapper.endContext(contextEndingEvent);
        flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, movement);
    }

    /**
     * CORNER CASE:  For a context with a special early-terminated end date that is different than normal,
     * this special ending needs to be carried over to the proper future frame, and dropped in the right place
     */

    public void endContextLater(ContextEndingEvent exitContextEvent) {
        this.contextMapper.endContextWhenInWindow(exitContextEvent);
    }

    /**
     * Walk through a sequence of events, and the StoryFrame will build a model of locations in space,
     * and movements through time, that can be played back like music
     */

    public void gotoLocation(LocalDateTime moment, String thoughtName, String locationPath, Duration timeInLocation) {

        List<Movement> movements = spatialGeometryMapper.gotoLocation(moment, thoughtName, locationPath, timeInLocation);
        flowRhythmMapper.addMovements(RhythmLayerType.LOCATION_CHANGES, movements);
    }

    /**
     * Modification activity is aggregated by the SpatialGeometryMapper for each location,
     * to get an overall idea of how much modification is happening,
     * then the FlowRhythmMapper captures the rhythm of modification, the starts and stops,
     * as a heuristic detection of experienced friction
     */

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {

        spatialGeometryMapper.modifyCurrentLocation(modificationCount);
        flowRhythmMapper.modifyCurrentLocation(moment, modificationCount);
    }


    /**
     * Execution rhythms are mapped by the FlowRhythmMapper to look for patterns of red/green bar
     * changes, and patterns in execution cycles.  The execution context details are passed in
     *
     * @param moment
     * @param executionContext
     */
    public void execute(LocalDateTime moment, ExecutionContext executionContext) {
        flowRhythmMapper.execute(moment, executionContext);
    }



    public void postCircleMessage(LocalDateTime moment, CircleMessageContext messageContext) {
        flowRhythmMapper.postCircleMessage(moment, messageContext);
    }

    /**
     * Start a time band at the position, that will continue for the duration, including carry over into
     * subsequent frames until the band is cleared
     * @param bandLayerType
     * @param startBandPosition
     * @param bandContext
     */
    public void startBand(BandLayerType bandLayerType, LocalDateTime startBandPosition, BandContext bandContext) {
        timeBandMapper.startBand(bandLayerType, startBandPosition, bandContext);
    }

    /**
     * Clear the active time band in this layer, from the specified time onward, no bands will be generated
     * @param bandLayerType
     * @param endBandPosition
     */
    public void clearBand(BandLayerType bandLayerType, LocalDateTime endBandPosition) {
        timeBandMapper.clearBand(bandLayerType, endBandPosition);
    }

    /**
     * After filling in a StoryFrame with a layer of stuff, call this with each layer to put the frame
     * back into a good final state
     */

    public void finishAfterLoad() {
        contextMapper.finish();
        spatialGeometryMapper.finish();
        flowRhythmMapper.finish();
        timeBandMapper.finish();
    }

    /**
     * When a new frame is initialized by "panning right", forwarding one step into the future
     * The last context of the prior frame becomes the starting context of the new frame.
     *
     * This orchestration is handled by the StoryFrameSequence
     *
     * @param previousStoryFrame
     */

    public void carryOverFrameContext(StoryFrame previousStoryFrame) {
        CarryOverContext carryOverContext = previousStoryFrame.getCarryOverContext();

        this.spatialGeometryMapper.initFromCarryOverContext(carryOverContext);
        this.flowRhythmMapper.initFromCarryOverContext(carryOverContext);
        this.timeBandMapper.initFromCarryOverContext(carryOverContext);

        Movement sideEffectMovement = this.contextMapper.initFromCarryOverContext(carryOverContext);
        this.flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, sideEffectMovement);
    }

    public CarryOverContext getCarryOverContext() {

        CarryOverContext carryOverContext = new CarryOverContext("[StoryFrame]");

        carryOverContext.addSubContext(contextMapper.getCarryOverContext());
        carryOverContext.addSubContext(spatialGeometryMapper.getCarryOverContext());
        carryOverContext.addSubContext(flowRhythmMapper.getCarryOverContext());
        carryOverContext.addSubContext(timeBandMapper.getCarryOverContext());

        return carryOverContext;
    }

    //////////// Extract all the various state for persistence ////////////

    public OuterGeometryClock.Coords getStoryFrameCoordinates() {
        return storyFrameCoordinates;
    }

    public ZoomLevel getZoomLevel() {
        return zoomLevel;
    }

    public InnerGeometryClock.Coords getCurrentMoment() {
        return this.flowRhythmMapper.getCurrentMoment();
    }

    public ContextSummary getCurrentContext() {
        return this.contextMapper.getCurrentContextSummary();
    }

    public FocusPlace getCurrentFocalPoint() {
        return spatialGeometryMapper.getCurrentFocusPlace();
    }

    public LocationInPlace getCurrentLocationInFocus() {
        return spatialGeometryMapper.getCurrentLocation();
    }

    public BoxAndBridgeStructure getThoughtStructure() {
        return spatialGeometryMapper.getThoughtStructure();
    }

    public Set<RhythmLayerType> getRhythmLayerTypes() {
        return flowRhythmMapper.getRhythmLayerTypes();
    }

    public Set<BandLayerType> getBandLayerTypes() {
        return timeBandMapper.getBandLayerTypes();
    }

    public RhythmLayer getRhythmLayer(RhythmLayerType layerType) {
        return flowRhythmMapper.getRhythmLayer(layerType);
    }

    public TimeBandLayer getBandLayer(BandLayerType layerType) {
        return timeBandMapper.getBandLayer(layerType);
    }

    public Movement getLastMovement(RhythmLayerType rhythmLayerType) {
        return flowRhythmMapper.getLastMovement(rhythmLayerType);
    }

    public TimeBand getLastBand(BandLayerType bandLayerType) {
        return timeBandMapper.getLastBand(bandLayerType);
    }



}
