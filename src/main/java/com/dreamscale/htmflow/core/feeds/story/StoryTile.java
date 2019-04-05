package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.*;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.dreamscale.htmflow.core.feeds.story.feature.details.MessageDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.context.*;
import com.dreamscale.htmflow.core.feeds.story.feature.details.ExecutionDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.BoxAndBridgeStructure;
import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInFocus;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.FocalPoint;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class StoryTile {

    private final OuterGeometryClock.Coords storyFrameCoordinates;
    private final ZoomLevel zoomLevel;

    private final InnerGeometryClock internalClock;

    private final FlowContextMapper contextMapper;
    private final SpatialGeometryMapper spatialGeometryMapper;
    private final FlowRhythmMapper flowRhythmMapper;
    private final FlowBandMapper timeBandMapper;


    public StoryTile(OuterGeometryClock.Coords storyFrameCoordinates, ZoomLevel zoomLevel) {
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

    public void beginContext(ContextBeginning contextBeginning) {
        Movement movement = contextMapper.beginContext(contextBeginning);
        flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, movement);
    }

    /**
     * End an active context, such as project, task, or intention
     */

    public void endContext(ContextEnding contextEnding) {
        Movement movement = contextMapper.endContext(contextEnding);
        flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, movement);
    }

    /**
     * CORNER CASE:  For a context with a special early-terminated end date that is different than normal,
     * this special ending needs to be carried over to the proper future frame, and dropped in the right place
     */

    public void endContextLater(ContextEnding exitContextEvent) {
        this.contextMapper.endContextWhenInWindow(exitContextEvent);
    }

    /**
     * Walk through a sequence of events, and the StoryFrame will build a model of locations in space,
     * and movements through time, that can be played back like music
     */

    public void gotoLocation(LocalDateTime moment, String placeName, String locationPath, Duration timeInLocation) {

        List<Movement> movements = spatialGeometryMapper.gotoLocation(moment, placeName, locationPath, timeInLocation);
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
     * @param executionDetails
     */
    public void execute(LocalDateTime moment, ExecutionDetails executionDetails) {
        flowRhythmMapper.execute(moment, executionDetails);
    }



    public void postMessage(LocalDateTime moment, MessageDetails messageDetails) {
        flowRhythmMapper.postMessage(moment, messageDetails);
    }

    /**
     * Start a time band at the position, that will continue for the duration, including carry over into
     * subsequent frames until the band is cleared
     * @param bandLayerType
     * @param startBandPosition
     * @param details
     */
    public void startBand(BandLayerType bandLayerType, LocalDateTime startBandPosition, Details details) {
        timeBandMapper.startBand(bandLayerType, startBandPosition, details);
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
     * @param previousStoryTile
     */

    public void carryOverFrameContext(StoryTile previousStoryTile) {
        CarryOverContext carryOverContext = previousStoryTile.getCarryOverContext();

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

    public FocalPoint getCurrentFocalPoint() {
        return spatialGeometryMapper.getCurrentFocusPlace();
    }

    public LocationInFocus getCurrentLocationInFocus() {
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
