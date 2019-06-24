package com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.fetch;

import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.source.Bookmark;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;

import java.util.UUID;

public abstract class FetchStrategy<T extends Flowable> {

    public abstract Batch<T> fetchNextBatch(UUID memberId, Bookmark bookmarkPosition, int fetchSize);
}
