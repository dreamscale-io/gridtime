package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed;

import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source.Bookmark;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;

import java.util.UUID;

public abstract class FeedStrategy<T extends Flowable> {

    public abstract Batch<T> fetchNextBatch(UUID memberId, Bookmark bookmarkPosition, int fetchSize);
}
