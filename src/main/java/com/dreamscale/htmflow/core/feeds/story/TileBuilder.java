package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.pool.FeatureCache;
import com.dreamscale.htmflow.core.feeds.story.feature.*;
import com.dreamscale.htmflow.core.feeds.story.feature.details.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.*;
import com.dreamscale.htmflow.core.feeds.story.feature.context.*;
import com.dreamscale.htmflow.core.feeds.story.grid.TileGrid;
import com.dreamscale.htmflow.core.feeds.story.grid.TileGridModel;
import com.dreamscale.htmflow.core.feeds.story.music.BeatSize;
import com.dreamscale.htmflow.core.feeds.story.music.clock.ClockBeat;
import com.dreamscale.htmflow.core.feeds.story.music.clock.MusicClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import com.dreamscale.htmflow.core.feeds.story.mapper.*;
import com.dreamscale.htmflow.core.feeds.story.music.TileGridPlayer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class TileBuilder {


    private final String tileUri;
    private final GeometryClock.Coords tileCoordinates;
    private final ZoomLevel zoomLevel;

    private final FlowContextMapper contextMapper;
    private final SpatialGeometryMapper spatialGeometryMapper;
    private final FlowRhythmMapper flowRhythmMapper;
    private final FlowBandMapper timeBandMapper;

    private final FeatureFactory featureFactory;
    private final TileGrid tileGrid;
    private final MusicClock musicClock;


    public TileBuilder(String feedUri, GeometryClock.Coords tileCoordinates, ZoomLevel zoomLevel) {
        this.tileCoordinates = tileCoordinates;
        this.zoomLevel = zoomLevel;
        this.tileUri = TileUri.createTileUri(feedUri, zoomLevel, tileCoordinates);

        LocalDateTime from = tileCoordinates.getClockTime();
        LocalDateTime to = tileCoordinates.panRight(zoomLevel).getClockTime();

        this.featureFactory = new FeatureFactory(tileUri);
        this.musicClock = new MusicClock(from, to, 20);

        this.contextMapper = new FlowContextMapper(featureFactory, musicClock);
        this.flowRhythmMapper = new FlowRhythmMapper(featureFactory, musicClock);
        this.timeBandMapper = new FlowBandMapper(featureFactory, musicClock);


        this.tileGrid = new TileGrid(musicClock, BeatSize.QUARTER);


        this.spatialGeometryMapper = new SpatialGeometryMapper(featureFactory, tileGrid);

    }




    /**
     * Change the active context, such as starting project, task, or intention
     */

    public void beginContext(MusicalSequenceBeginning contextBeginning) {
        Movement movement = contextMapper.beginContext(contextBeginning);

        flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, movement);
    }

    /**
     * End an active context, such as project, task, or intention
     */

    public void endContext(MusicalSequenceEnding contextEnding) {


        Movement movement = contextMapper.endContext(contextEnding);
        flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, movement);
    }

    /**
     * CORNER CASE:  For a context with a special early-terminated end date that is different than normal,
     * this special ending needs to be carried over to the proper future frame, and dropped in the right place
     */

    public void endContextLater(MusicalSequenceEnding exitContextEvent) {
        this.contextMapper.endContextWhenInWindow(exitContextEvent);
    }

    /**
     * Walk through a sequence of events, and the StoryFrame will build a model of locations in space,
     * and movements through time, that can be played back like music
     */

    public void gotoLocation(LocalDateTime moment, String boxName, String locationPath, Duration timeInLocation) {

        List<Movement> movements = spatialGeometryMapper.gotoLocation(moment, boxName, locationPath, timeInLocation);

        flowRhythmMapper.addMovements(RhythmLayerType.LOCATION_CHANGES, movements);

        Box currentBox = spatialGeometryMapper.getCurrentBox();
        LocationInBox currentLocation = spatialGeometryMapper.getCurrentLocation();
        Traversal lastTraversal = spatialGeometryMapper.getLastTraversal();
        Bridge recentBridgeCrossed = spatialGeometryMapper.getRecentBridgeCrossed();

        ClockBeat clockBeat = musicClock.getClosestBeat(moment);

        tileGrid.getMetricsFor(currentBox, clockBeat).addVelocitySample(timeInLocation);
        tileGrid.getMetricsFor(currentLocation, clockBeat).addVelocitySample(timeInLocation);
        tileGrid.getMetricsFor(lastTraversal, clockBeat).addVelocitySample(timeInLocation);

        if (recentBridgeCrossed != null) {
            tileGrid.getMetricsFor(recentBridgeCrossed, clockBeat).addVelocitySample(timeInLocation);
        }

        MomentOfContext context = contextMapper.getMomentOfContext(moment);
        tileGrid.getMetricsFor(context.getProjectContext(), clockBeat).addVelocitySample(timeInLocation);
        tileGrid.getMetricsFor(context.getTaskContext(), clockBeat).addVelocitySample(timeInLocation);
        tileGrid.getMetricsFor(context.getIntentionContext(), clockBeat).addVelocitySample(timeInLocation);
    }

    /**
     * Modification activity is aggregated to get an overall idea of how much modification is happening
     */

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {

        Box currentBox = spatialGeometryMapper.getCurrentBox();
        LocationInBox currentLocation = spatialGeometryMapper.getCurrentLocation();

        ClockBeat clockBeat = musicClock.getClosestBeat(moment);

        tileGrid.getMetricsFor(currentBox, clockBeat).addModificationSample(modificationCount);
        tileGrid.getMetricsFor(currentLocation, clockBeat).addModificationSample(modificationCount);

        MomentOfContext context = contextMapper.getMomentOfContext(moment);

        tileGrid.getMetricsFor(context.getProjectContext(), clockBeat).addModificationSample(modificationCount);
        tileGrid.getMetricsFor(context.getTaskContext(), clockBeat).addModificationSample(modificationCount);
        tileGrid.getMetricsFor(context.getIntentionContext(), clockBeat).addModificationSample(modificationCount);
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

        flowRhythmMapper.executeThing(moment, executionDetails);

        ClockBeat clockBeat = musicClock.getClosestBeat(moment);

        Box currentBox = spatialGeometryMapper.getCurrentBox();
        tileGrid.getMetricsFor(currentBox, clockBeat).addExecutionSample(executionDetails.getDuration());

        tileGrid.getMetricsFor(context.getProjectContext(), clockBeat).addExecutionSample(executionDetails.getDuration());
        tileGrid.getMetricsFor(context.getTaskContext(), clockBeat).addExecutionSample(executionDetails.getDuration());
        tileGrid.getMetricsFor(context.getIntentionContext(), clockBeat).addExecutionSample(executionDetails.getDuration());

        if (executionDetails.isRedAndWantingGreen()) {
            tileGrid.getMetricsFor(currentBox, clockBeat).addExecutionCycleTimeSample(executionDetails.getDurationUntilNextExecution());

            tileGrid.getMetricsFor(context.getProjectContext(), clockBeat).addExecutionSample(executionDetails.getDuration());
            tileGrid.getMetricsFor(context.getTaskContext(), clockBeat).addExecutionSample(executionDetails.getDuration());
            tileGrid.getMetricsFor(context.getIntentionContext(), clockBeat).addExecutionSample(executionDetails.getDuration());
        }

    }


    /**
     * Add an event for posting a message to the circle to help solve a problem
     * @param moment
     * @param messageDetails
     */
    public void postCircleMessage(LocalDateTime moment, MessageDetails messageDetails) {
        MomentOfContext context = contextMapper.getMomentOfContext(moment);
        flowRhythmMapper.shareMessage(moment, messageDetails);
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
     * An alternative set of authors is active for all subsequent data until changed
     * @param startBandPosition
     * @param authorDetails
     */
    public void startAuthorsBand(LocalDateTime startBandPosition, AuthorDetails authorDetails) {
        timeBandMapper.startBand(BandLayerType.AUTHORS, startBandPosition, authorDetails);
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
        TileGridPlayer storyPlayer = new TileGridPlayer(tileGrid);

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
     * Extract a serializable form of the generated model, that can be compared with other tiles,
     * and loaded into grids
     */
    public StoryTileModel extractStoryTileModel() {
        StoryTileModel model = new StoryTileModel();
        model.setTileUri(getTileUri());
        model.setZoomLevel(getZoomLevel());
        model.setTileCoordinates(getTileCoordinates());

        model.setMomentsOfContext(getAllContexts());
        model.setBandLayers(getBandLayers());
        model.setRhythmLayers(getRhythmLayers());
        model.setSpatialStructure(getSpatialStructure());
        model.setStoryTileSummary(getStoryTileSummary());
        model.setStoryGrid(getTileGrid());
        model.setCarryOverContext(getCarryOverContext());

        return model;
    }


    /**
     * When a new frame is initialized by "panning right", forwarding one step into the future
     * The last context of the prior frame becomes the starting context of the new frame.
     *
     * This orchestration is handled by the StoryFrameSequence
     *
     */

    public void carryOverTileContext(CarryOverContext carryOverContext) {

        this.spatialGeometryMapper.initFromCarryOverContext(carryOverContext);
        this.flowRhythmMapper.initFromCarryOverContext(carryOverContext);
        this.timeBandMapper.initFromCarryOverContext(carryOverContext);

        Movement sideEffectMovement = this.contextMapper.initFromCarryOverContext(carryOverContext);

        this.flowRhythmMapper.addMovement(RhythmLayerType.CONTEXT_CHANGES, sideEffectMovement);
    }

    public CarryOverContext getCarryOverContext() {

        CarryOverContext carryOverContext = new CarryOverContext("[StoryTile]");

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

    public BoxAndBridgeActivity getSpatialStructure() {
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

    public TileGridModel getTileGrid() {
        return tileGrid.getStoryGridModel();
    }

    public StoryTileSummary getStoryTileSummary() {
        return new StoryTileSummary(tileGrid.getStoryGridModel());
    }

    public List<MomentOfContext> getAllContexts() {
        return contextMapper.getAllContexts();
    }

    public MomentOfContext getCurrentContext() {
        return contextMapper.getCurrentContext();
    }

    public MomentOfContext getContextOfMoment(LocalDateTime moment) {
        return contextMapper.getMomentOfContext(moment);
    }


    public MomentOfContext getInitialContext() {
        return contextMapper.getInitialContext();
    }

    public LocalDateTime getStart() {
        return musicClock.getFromClockTime();
    }

    public LocalDateTime getEnd() {
        return musicClock.getToClockTime();
    }
}
