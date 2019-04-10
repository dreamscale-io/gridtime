package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.details.*;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.*;
import com.dreamscale.htmflow.core.feeds.story.feature.context.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.BoxAndBridgeStructure;
import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class StoryFrame {

    private final GeometryClock.Coords frameCoordinates;
    private final ZoomLevel zoomLevel;

    private final MusicGeometryClock internalClock;

    private final FlowContextMapper contextMapper;
    private final SpatialGeometryMapper spatialGeometryMapper;
    private final FlowRhythmMapper flowRhythmMapper;
    private final FlowBandMapper timeBandMapper;
    private final String frameUri;


    public StoryFrame(String feedUri, GeometryClock.Coords frameCoordinates, ZoomLevel zoomLevel) {
        this.frameCoordinates = frameCoordinates;
        this.zoomLevel = zoomLevel;

        this.frameUri = StandardizedKeyMapper.createFrameUri(feedUri, zoomLevel, frameCoordinates);

        this.internalClock = new MusicGeometryClock(
                frameCoordinates.getClockTime(),
                frameCoordinates.panRight(zoomLevel).getClockTime());

        this.contextMapper = new FlowContextMapper(internalClock.getFromClockTime(), internalClock.getToClockTime());
        this.spatialGeometryMapper = new SpatialGeometryMapper(internalClock.getFromClockTime(), internalClock.getToClockTime());
        this.flowRhythmMapper = new FlowRhythmMapper(internalClock.getFromClockTime(), internalClock.getToClockTime());
        this.timeBandMapper = new FlowBandMapper(internalClock.getFromClockTime(), internalClock.getToClockTime());

    }

    /**
     * Change the active context, such as starting project, task, or intention
     */

    public void beginContext(ContextChangeEvent contextBeginning) {
        Movement movement = contextMapper.beginContext(contextBeginning);
        ContextSummary context = contextMapper.getContextOfMoment(contextBeginning.getPosition());
        flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, context, movement);
    }

    /**
     * End an active context, such as project, task, or intention
     */

    public void endContext(ContextChangeEvent contextEnding) {
        Movement movement = contextMapper.endContext(contextEnding);
        ContextSummary context = contextMapper.getContextOfMoment(contextEnding.getPosition());
        flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, context, movement);
    }

    /**
     * CORNER CASE:  For a context with a special early-terminated end date that is different than normal,
     * this special ending needs to be carried over to the proper future frame, and dropped in the right place
     */

    public void endContextLater(ContextChangeEvent exitContextEvent) {
        this.contextMapper.endContextWhenInWindow(exitContextEvent);
    }

    /**
     * Walk through a sequence of events, and the StoryFrame will build a model of locations in space,
     * and movements through time, that can be played back like music
     */

    public void gotoLocation(LocalDateTime moment, String placeName, String locationPath, Duration timeInLocation) {

        List<Movement> movements = spatialGeometryMapper.gotoLocation(moment, placeName, locationPath, timeInLocation);

        ContextSummary context = contextMapper.getContextOfMoment(moment);
        flowRhythmMapper.addMovements(RhythmLayerType.LOCATION_CHANGES, context, movements);
    }

    /**
     * Modification activity is aggregated by the SpatialGeometryMapper for each location,
     * to get an overall idea of how much modification is happening
     */

    public void modifyCurrentLocation(int modificationCount) {
        spatialGeometryMapper.modifyCurrentLocation(modificationCount);
    }


    /**
     * Execution rhythms are mapped by the FlowRhythmMapper to look for patterns of red/green bar
     * changes, and patterns in execution cycles.  The execution context details are passed in
     *
     * @param moment
     * @param executionDetails
     */
    public void executeThing(LocalDateTime moment, ExecutionDetails executionDetails) {
        ContextSummary context = contextMapper.getContextOfMoment(moment);
        flowRhythmMapper.executeThing(moment, context, executionDetails);
    }


    /**
     * Add an event for posting a message to the circle to help solve a problem
     * @param moment
     * @param message
     */
    public void postCircleMessage(LocalDateTime moment, Message message) {
        ContextSummary context = contextMapper.getContextOfMoment(moment);
        flowRhythmMapper.shareMessage(moment, context, message);
    }

    /**
     * Starts a WTF friction band...
     * @param startBandPosition
     * @param circleDetails
     */
    public void startWTF(LocalDateTime startBandPosition, CircleDetails circleDetails) {
        timeBandMapper.startBand(BandLayerType.FRICTION_WTF, startBandPosition, circleDetails);
    }

    /**
     * Clears a WTF friction band...
     */
    public void clearWTF(LocalDateTime endBandPosition) {
        timeBandMapper.clearBand(BandLayerType.FRICTION_WTF, endBandPosition);
    }

    /**
     * An alternative set of authors is active for all subsequent data until cleared
     * @param startBandPosition
     * @param authorDetails
     */
    public void startAlternativeAuthorsBand(LocalDateTime startBandPosition, AuthorDetails authorDetails) {
        timeBandMapper.startBand(BandLayerType.PAIRING_AUTHORS, startBandPosition, authorDetails);
    }

    /**
     * Clear out the alternative authors, and go back to default
     * @param endBandPosition
     */
    public void clearAlternativeAuthorsBand(LocalDateTime endBandPosition) {
        timeBandMapper.clearBand(BandLayerType.PAIRING_AUTHORS, endBandPosition);
    }

    /**
     * Activate the feels flame rating from this point forward until cleared or changed
     * @param startBandPosition
     * @param feelsDetails
     */
    public void startFeelsBand(LocalDateTime startBandPosition, FeelsDetails feelsDetails) {
        timeBandMapper.startBand(BandLayerType.FEELS, startBandPosition, feelsDetails);
    }

    /**
     * Clear out the feels flame rating, and go back to default
     * @param endBandPosition
     */
    public void clearFeelsBand(LocalDateTime endBandPosition) {
        timeBandMapper.clearBand(BandLayerType.FEELS, endBandPosition);
    }

    /**
     * Learning time vs Progress is determined by sampling the typing activity to identify when
     * lots of navigating around and reading shifts to a state of modifying things.  Learning Friction is
     * estimated by the amount of time it takes to shift into a regular cadence of typing
     */
    public void addTypingSampleToAssessLearningFriction(LocalDateTime moment, int modificationCount) {
        timeBandMapper.addRollingBandSample(BandLayerType.FRICTION_LEARNING, moment, modificationCount);
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

        ContextSummary context = this.contextMapper.getCurrentContext();

        this.flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, context, sideEffectMovement);
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

    public GeometryClock.Coords getFrameCoordinates() {
        return frameCoordinates;
    }

    public ZoomLevel getZoomLevel() {
        return zoomLevel;
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

    public List<RhythmLayer> getRhythmLayers() {
        return flowRhythmMapper.getRhythmLayers();
    }

    public TimeBandLayer getBandLayer(BandLayerType layerType) {
        return timeBandMapper.getBandLayer(layerType);
    }

    public List<TimeBandLayer> getBandLayers() {
        return timeBandMapper.getBandLayers();
    }

    public Movement getLastMovement(RhythmLayerType rhythmLayerType) {
        return flowRhythmMapper.getLastMovement(rhythmLayerType);
    }

    public String getFrameUri() {
        return frameUri;
    }


    public ContextSummary getCurrentContext() {
        return contextMapper.getCurrentContext();
    }

    public ContextSummary getContextOfMoment(LocalDateTime moment) {
        return contextMapper.getContextOfMoment(moment);
    }
}
