package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
public class MusicalSequenceEnding extends FlowFeature implements ContextChangeEvent {

    private LocalDateTime position;
    private int relativeSequence;
    private FinishStatus finishStatus;

    private Context context;

    public MusicalSequenceEnding() {
        super(FlowObjectType.MUSIC_ENDING_EVENT);

        context = new Context();
    }

    public MusicalSequenceEnding(LocalDateTime position, StructureLevel structureLevel, UUID referenceId) {
        super(FlowObjectType.MUSIC_ENDING_EVENT);

        this.position = position;
        this.context = new Context();
        context.setStructureLevel(structureLevel);
        context.setId(referenceId);
    }


    @Override
    public StructureLevel getStructureLevel() {
        return context.getStructureLevel();
    }

    public void setReferenceId(UUID id) {
        context.setId(id);
    }

    public UUID getReferenceId() { return context.getId(); }

    public void setStructureLevel(StructureLevel structureLevel) {
        context.setStructureLevel(structureLevel);
    }

    public void setDescription(String description) {
        context.setDescription(description);
    }

    public enum FinishStatus {
        SUCCESS, ABORT
    }
}
