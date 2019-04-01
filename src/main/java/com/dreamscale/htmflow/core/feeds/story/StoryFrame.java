package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.structure.BoxAndBridgeStructure;
import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextEndingEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.FlowStructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInFocus;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.FocalPoint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class StoryFrame {

    private final OuterGeometryClock.Coords storyCoordinates;
    private final ZoomLevel zoomLevel;

    private final InnerGeometryClock internalClock;

    private final FlowContextMapper contextMapper;
    private final SpatialGeometryMapper spatialGeometryMapper;
    private final FlowRhythmMapper flowRhythmMapper;


    //this is to support a corner case for carrying over context endings from prior frames
    private ContextEndingEvent savedContextToAddWhenInWindow;

    public StoryFrame(OuterGeometryClock.Coords storyCoordinates, ZoomLevel zoomLevel) {
        this.storyCoordinates = storyCoordinates;
        this.zoomLevel = zoomLevel;

        this.internalClock = new InnerGeometryClock(
                storyCoordinates.getClockTime(),
                storyCoordinates.panRight(zoomLevel).getClockTime());

        this.contextMapper = new FlowContextMapper();
        this.spatialGeometryMapper = new SpatialGeometryMapper();
        this.flowRhythmMapper = new FlowRhythmMapper(storyCoordinates, zoomLevel);

    }

    /**
     * Change the active context, such as starting project, task, or intention
     */

    public void beginContext(ContextBeginningEvent contextBeginningEvent) {
        MovementEvent movement = contextMapper.beginContext(contextBeginningEvent);
        flowRhythmMapper.addMovement(FlowLayerType.CONTEXT_CHANGES, movement);
    }

    /**
     * End an active context, such as project, task, or intention
     */

    public void endContext(ContextEndingEvent contextEndingEvent) {
        MovementEvent movement = contextMapper.endContext(contextEndingEvent);
        flowRhythmMapper.addMovement(FlowLayerType.CONTEXT_CHANGES, movement);
    }

    /**
     * Walk through a sequence of events, and the StoryFrame will build a model of locations in space,
     * and movements through time, that can be played back like music
     */

    public void gotoLocation(LocalDateTime moment, String thoughtName, String locationPath, Duration timeInLocation) {

        List<MovementEvent> movements = spatialGeometryMapper.gotoLocation(moment, thoughtName, locationPath, timeInLocation);
        flowRhythmMapper.addMovements(FlowLayerType.LOCATION_CHANGES, movements);
    }

    /**
     * Modification activity is aggregated by the SpatialGeometryMapper for each location,
     * to get an overall idea of how much modification is happening,
     * then the FlowSequenceMapper captures the rhythm of modification, the starts and stops,
     * as a heuristic detection of experienced friction
     */

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {

        spatialGeometryMapper.modifyCurrentLocation(modificationCount);
        flowRhythmMapper.modifyCurrentLocation(moment, modificationCount);
    }

    /**
     * After filling in a StoryFrame with a layer of stuff, call this with each layer to put the frame
     * back into a good final state
     */

    public void finishStoryFrameAfterLoad() {
        contextMapper.finish();
        spatialGeometryMapper.finish();
        flowRhythmMapper.finish();
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
        FocalPoint previousFocalPoint = previousStoryFrame.getCurrentFocalPoint();
        LocationInFocus previousLocation = previousStoryFrame.getCurrentLocationInThought();

        this.spatialGeometryMapper.initPlaceFromPriorContext(previousFocalPoint, previousLocation);
        this.flowRhythmMapper.initLayerSequencesFromPriorContext(previousStoryFrame.getLayerSequences());

        carryOverFrameContext(FlowStructureLevel.PROJECT, previousStoryFrame);
        carryOverFrameContext(FlowStructureLevel.TASK, previousStoryFrame);
        carryOverFrameContext(FlowStructureLevel.INTENTION, previousStoryFrame);

        saveContextToAddWhenInWindow(previousStoryFrame.getSavedContextToAddWhenInWindow());
    }

    private void carryOverFrameContext(FlowStructureLevel structureLevel, StoryFrame previousStoryFrame) {

        this.contextMapper.initContextFromPriorContext(previousStoryFrame.getCurrentContext(structureLevel));
        this.contextMapper.initSequenceFromPriorContext(structureLevel, previousStoryFrame.getCurrentRelativePosition(structureLevel));

    }

    private int getCurrentRelativePosition(FlowStructureLevel structureLevel) {
        return this.contextMapper.getCurrentSequenceNumber(structureLevel);
    }

    private Map<FlowLayerType, RelativeSequence> getLayerSequences() {
        return this.flowRhythmMapper.getLayerSequences();
    }

    /**
     * CORNER CASE:  For an activity with a special early-terminated end date that is different than
     * "when the next thing start", this special ending may need to be carried over to the next frame,
     * or forwarded several frames, to save the ending in the right place.
     *
     * Set the exit context event here, so it can be "picked up" again by the FlowObserver
     * within the proper frame.
     *
     * @param exitContextEvent
     *
     */
    public void saveContextToAddWhenInWindow(ContextEndingEvent exitContextEvent) {
        this.savedContextToAddWhenInWindow = exitContextEvent;
    }


    public ContextEndingEvent getSavedContextToAddWhenInWindow() {
        return savedContextToAddWhenInWindow;
    }

    //////////// Properties that describe the Active State ////////////

    public OuterGeometryClock.Coords getStoryCoordinates() {
        return storyCoordinates;
    }

    public ZoomLevel getZoomLevel() {
        return zoomLevel;
    }

    public FocalPoint getCurrentFocalPoint() {
        return spatialGeometryMapper.getCurrentPlace();
    }

    public LocationInFocus getCurrentLocationInThought() {
        return spatialGeometryMapper.getCurrentLocationInPlace();
    }

    public ContextBeginningEvent getCurrentContext(FlowStructureLevel structureLevel) {
        return this.contextMapper.getCurrentContext(structureLevel);
    }

    public InnerGeometryClock.Coords getCurrentMoment() {
        return this.flowRhythmMapper.getCurrentMoment();
    }

    public List<MovementEvent> getContextMovements() {
        return flowRhythmMapper.getContextMovements();
    }

    public BoxAndBridgeStructure getThoughtStructure() {
        return spatialGeometryMapper.getThoughtStructure();
    }


}
