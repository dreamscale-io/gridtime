package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch;

import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source.Bookmark;

import java.util.UUID;

public abstract class FetchStrategy {

    public abstract Batch fetchNextBatch(UUID memberId, Bookmark bookmarkPosition, int fetchSize);
}
