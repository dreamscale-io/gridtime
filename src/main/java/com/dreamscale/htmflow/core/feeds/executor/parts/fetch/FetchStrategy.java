package com.dreamscale.htmflow.core.feeds.executor.parts.fetch;

import com.dreamscale.htmflow.core.feeds.executor.parts.source.Bookmark;

import java.util.UUID;

public abstract class FetchStrategy {

    public abstract Batch fetchNextBatch(UUID memberId, Bookmark bookmarkPosition, int fetchSize);
}
