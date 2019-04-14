package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.details.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.*;
import com.dreamscale.htmflow.core.feeds.story.feature.context.*;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGrid;
import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.*;
import com.dreamscale.htmflow.core.feeds.story.music.Snapshot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class StoryTile {

    private final GeometryClock.Coords tileCoordinates;
    private final ZoomLevel zoomLevel;

    private final MusicGeometryClock internalClock;

    private final FlowContextMapper contextMapper;
    private final SpatialGeometryMapper spatialGeometryMapper;
    private final FlowRhythmMapper flowRhythmMapper;
    private final FlowBandMapper timeBandMapper;
    private final StoryMusicPlayer storyPlayer;

    private final String tileUri;

    private final FeatureFactory featureFactory;
    private final StoryGrid storyGrid;


    public StoryTile(String feedUri, GeometryClock.Coords tileCoordinates, ZoomLevel zoomLevel) {
        this.tileCoordinates = tileCoordinates;
        this.zoomLevel = zoomLevel;
        this.tileUri = URIMapper.createTileUri(feedUri, zoomLevel, tileCoordinates);

        this.internalClock = new MusicGeometryClock(
                tileCoordinates.getClockTime(),
                tileCoordinates.panRight(zoomLevel).getClockTime());

        this.featureFactory = new FeatureFactory(tileUri);
        this.storyGrid = new StoryGrid(internalClock);

        this.contextMapper = new FlowContextMapper(featureFactory, internalClock);
        this.spatialGeometryMapper = new SpatialGeometryMapper(featureFactory, storyGrid);
        this.flowRhythmMapper = new FlowRhythmMapper(featureFactory, internalClock);
        this.timeBandMapper = new FlowBandMapper(featureFactory, internalClock);

        this.storyPlayer = new StoryMusicPlayer(featureFactory, storyGrid, internalClock);

    }

    /**
     * Change the active context, such as starting project, task, or intention
     */

    public void beginContext(ContextBeginningEvent contextBeginning) {
        Movement movement = contextMapper.beginContext(contextBeginning);
        MomentOfContext context = contextMapper.getMomentOfContext(contextBeginning.getPosition());

        flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, context, movement);
    }

    /**
     * End an active context, such as project, task, or intention
     */

    public void endContext(ContextEndingEvent contextEnding) {
        Movement movement = contextMapper.endContext(contextEnding);
        MomentOfContext context = contextMapper.getMomentOfContext(contextEnding.getPosition());
        flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, context, movement);
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

    public void gotoLocation(LocalDateTime moment, String boxName, String locationPath, Duration timeInLocation) {

        MomentOfContext context = contextMapper.getMomentOfContext(moment);

        List<Movement> movements = spatialGeometryMapper.gotoLocation(moment, boxName, locationPath, timeInLocation);
        flowRhythmMapper.addMovements(RhythmLayerType.LOCATION_CHANGES, context, movements);

        Box currentBox = spatialGeometryMapper.getCurrentBox();
        LocationInBox currentLocation = spatialGeometryMapper.getCurrentLocation();
        Traversal lastTraversal = spatialGeometryMapper.getLastTraversal();
        Bridge recentBridgeCrossed = spatialGeometryMapper.getRecentBridgeCrossed();

        storyGrid.getMetricsFor(currentBox).addVelocitySample(timeInLocation);
        storyGrid.getMetricsFor(currentLocation).addVelocitySample(timeInLocation);
        storyGrid.getMetricsFor(lastTraversal).addVelocitySample(timeInLocation);

        if (recentBridgeCrossed != null) {
            storyGrid.getMetricsFor(recentBridgeCrossed).addVelocitySample(timeInLocation);
        }

        storyGrid.getMetricsFor(context.getProjectContext()).addVelocitySample(timeInLocation);
        storyGrid.getMetricsFor(context.getTaskContext()).addVelocitySample(timeInLocation);
        storyGrid.getMetricsFor(context.getIntentionContext()).addVelocitySample(timeInLocation);
    }

    /**
     * Modification activity is aggregated to get an overall idea of how much modification is happening
     */

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {

        MomentOfContext context = contextMapper.getMomentOfContext(moment);

        Box currentBox = spatialGeometryMapper.getCurrentBox();
        LocationInBox currentLocation = spatialGeometryMapper.getCurrentLocation();

        storyGrid.getMetricsFor(currentBox).addModificationSample(modificationCount);
        storyGrid.getMetricsFor(currentLocation).addModificationSample(modificationCount);

        storyGrid.getMetricsFor(context.getProjectContext()).addModificationSample(modificationCount);
        storyGrid.getMetricsFor(context.getTaskContext()).addModificationSample(modificationCount);
        storyGrid.getMetricsFor(context.getIntentionContext()).addModificationSample(modificationCount);
    }

    /**
     * Execution rhythms are mapped by the FlowRhythmMapper to look for patterns of red/green bar
     * changes, and patterns in execution cycles.  Execution times are aggregated across focus areas
     *
     * @param moment
     * @param executionDetails
     */
    public void executeThing(LocalDateTime moment, ExecutionDetails executionDetails) {
        MomentOfContext context = contextMapper.getMomentOfContext(moment);

        flowRhythmMapper.executeThing(moment, context, executionDetails);

        Box currentBox = spatialGeometryMapper.getCurrentBox();
        storyGrid.getMetricsFor(currentBox).addExecutionSample(executionDetails.getDuration());

        storyGrid.getMetricsFor(context.getProjectContext()).addExecutionSample(executionDetails.getDuration());
        storyGrid.getMetricsFor(context.getTaskContext()).addExecutionSample(executionDetails.getDuration());
        storyGrid.getMetricsFor(context.getIntentionContext()).addExecutionSample(executionDetails.getDuration());

    }


    /**
     * Add an event for posting a message to the circle to help solve a problem
     * @param moment
     * @param message
     */
    public void postCircleMessage(LocalDateTime moment, Message message) {
        MomentOfContext context = contextMapper.getMomentOfContext(moment);
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
     * Once the entire scene is loaded, play all the frames, mix the content so that feels bleed over
     * to locations, and snapshots are generated for each tick
     */
    public void play() {
        storyPlayer.loadFrame(this);
        storyPlayer.play();
    }

    /**
     * Extract all the internal objects generated into a list of serializable JSON features
     * @return
     */
    public List<FlowFeature> extractTemporalFeatures() {

        return featureFactory.getAllTemporalFeatures();

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

        MomentOfContext context = this.contextMapper.getCurrentContext();

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

    public GeometryClock.Coords getTileCoordinates() {
        return tileCoordinates;
    }

    public ZoomLevel getZoomLevel() {
        return zoomLevel;
    }

    public BoxAndBridgeActivity getSpatialStructuredActivity() {
        return spatialGeometryMapper.getSpatialStructuredActivity();
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

    public TimebandLayer getBandLayer(BandLayerType layerType) {
        return timeBandMapper.getBandLayer(layerType);
    }

    public List<TimebandLayer> getBandLayers() {
        return timeBandMapper.getBandLayers();
    }

    public Movement getLastMovement(RhythmLayerType rhythmLayerType) {
        return flowRhythmMapper.getLastMovement(rhythmLayerType);
    }

    public String getTileUri() {
        return tileUri;
    }

    public List<MomentOfContext> getAllContexts() {
        return contextMapper.getAllContexts();
    }

    public List<Snapshot> getSnapshots() {
        return storyPlayer.getSnapshots();
    }

    public MomentOfContext getCurrentContext() {
        return contextMapper.getCurrentContext();
    }

    public MomentOfContext getContextOfMoment(LocalDateTime moment) {
        return contextMapper.getMomentOfContext(moment);
    }




}
