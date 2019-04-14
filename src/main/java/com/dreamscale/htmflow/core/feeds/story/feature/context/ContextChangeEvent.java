package com.dreamscale.htmflow.core.feeds.story.feature.context;

import java.time.LocalDateTime;

public interface ContextChangeEvent {

    LocalDateTime getPosition();
    Context getContext();

    void setContext(Context context);

    StructureLevel getStructureLevel();
}
