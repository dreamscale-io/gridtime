package com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.fetch;

import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.source.Bookmark;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feed.Flowable;

import java.util.UUID;

public abstract class FetchStrategy<T extends Flowable> {

    public abstract Batch<T> fetchNextBatch(UUID memberId, Bookmark bookmarkPosition, int fetchSize);
}
