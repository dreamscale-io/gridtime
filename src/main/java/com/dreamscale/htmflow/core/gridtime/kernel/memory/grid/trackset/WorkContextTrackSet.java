package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.type.WorkContextType;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.WorkContextReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Set;


@Slf4j
public class WorkContextTrackSet extends MusicTrackSet<WorkContextType, WorkContextReference> {

    public WorkContextTrackSet(TrackSetName trackSetName, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        super(trackSetName, gridTime, musicClock);
    }

    public void startWorkContext(LocalDateTime moment, WorkContextReference workContext) {
        startPlaying(workContext.getWorkType(), moment, workContext);
    }

    public void clearWorkContext(LocalDateTime moment, FinishTag finishTag) {
        clearAllTracks(moment, finishTag);
    }


    public CarryOverContext getCarryOverContext(String subcontextName) {

        CarryOverContext carryOverContext = new CarryOverContext(subcontextName);

        Set<WorkContextType> types = super.getTrackTypes();

        for (WorkContextType type : types) {
            WorkContextReference lastContext = getLast(type);
            log.debug("Saving reference: "+lastContext);
            carryOverContext.saveReference(type.name(), lastContext);
        }

        return carryOverContext;
    }

    public void initFromCarryOverContext(CarryOverContext subContext) {
        Set<String> referenceKeys = subContext.getReferenceKeys();
        for (String referenceKey: referenceKeys) {
            WorkContextType type = WorkContextType.valueOf(referenceKey);
            initFirst(type, (WorkContextReference) subContext.getReference(referenceKey));
        }
    }

    public Set<? extends FeatureReference> getFeatures() {
        return super.getFeatures();
    }
}
