package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ContextEndingEvent extends FlowFeature implements ContextChangeEvent {

    private LocalDateTime position;
    private int relativeSequence;
    private FinishStatus finishStatus;

    private Context context = new Context();

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

    public void setName(String name) {
        context.setName(name);
    }

    public enum FinishStatus {
        SUCCESS, ABORT
    }
}
