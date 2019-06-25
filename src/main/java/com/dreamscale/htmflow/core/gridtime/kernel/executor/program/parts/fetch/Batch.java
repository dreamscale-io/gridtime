package com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.fetch;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.source.Bookmark;

import java.util.List;
import java.util.UUID;

public class Batch<T extends Flowable> {

    private final UUID memberId;
    private final Bookmark bookmarkUsedToSearch;
    private final List<T> flowables;

    public Batch(UUID memberId, Bookmark bookmarkUsedToSearch, List<T> flowables) {
        this.memberId = memberId;
        this.bookmarkUsedToSearch = bookmarkUsedToSearch;
        this.flowables = flowables;
    }

    public Bookmark getNextBookmark() {
        Bookmark bookmark = null;

        if (getFlowables().size() > 0) {
            bookmark = getFlowables().get(getFlowables().size() - 1).getBookmark();
            bookmark.nudgeForward();
        }
        return bookmark;
    }

    public List<T> getFlowables() {
        return flowables;
    }

    public int size() {
        return flowables.size();
    }
}
