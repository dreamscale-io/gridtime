package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch;

import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source.Bookmark;

import java.util.List;
import java.util.UUID;

public class Batch {

    private final UUID memberId;
    private final Bookmark bookmarkUsedToSearch;
    private final List<? extends Flowable> flowables;

    public Batch(UUID memberId, Bookmark bookmarkUsedToSearch, List<? extends Flowable> flowables) {
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

    public List<? extends Flowable> getFlowables() {
        return flowables;
    }

    public int size() {
        return flowables.size();
    }
}
