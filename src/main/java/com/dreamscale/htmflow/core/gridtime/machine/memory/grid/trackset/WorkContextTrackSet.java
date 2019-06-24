package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.WorkContextType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.WorkContextReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;


@Slf4j
public class WorkContextTrackSet extends MusicTrackSet<WorkContextType, WorkContextReference> {

    public WorkContextTrackSet(TrackSetName trackSetName, MusicClock musicClock) {
        super(trackSetName, musicClock);
    }

    public void startWorkContext(RelativeBeat beat, WorkContextReference workContext) {
        startPlaying(workContext.getWorkType(), beat, workContext);
    }

    public void clearWorkContext(RelativeBeat beat, FinishTag finishTag) {
        clearAllTracks(beat, finishTag);
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
