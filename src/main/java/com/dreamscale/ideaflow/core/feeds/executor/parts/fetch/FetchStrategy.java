package com.dreamscale.ideaflow.core.feeds.executor.parts.fetch;

import com.dreamscale.ideaflow.core.feeds.executor.parts.source.Bookmark;

import java.util.UUID;

public abstract class FetchStrategy {

    public abstract Batch fetchNextBatch(UUID memberId, Bookmark bookmarkPosition, int fetchSize);
}
