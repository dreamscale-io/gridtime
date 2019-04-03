package com.dreamscale.htmflow.core.feeds.executor.parts.fetch;

import com.dreamscale.htmflow.core.domain.JournalLinkEntity;
import com.dreamscale.htmflow.core.domain.JournalLinkRepository;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalLinkEvent;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Bookmark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class PairingEventFetcher extends FetchStrategy {

    @Autowired
    JournalLinkRepository journalLinkRepository;

    @Override
    public Batch fetchNextBatch(UUID memberId, Bookmark bookmark, int fetchSize) {

        LocalDateTime afterDate = bookmark.getPosition();

        List<JournalLinkEntity> journalLinkEntities =
                journalLinkRepository.findByMemberIdAfterDateWithLimit(memberId, Timestamp.valueOf(afterDate), fetchSize);

        List<Flowable> flowables = convertToFlowables(journalLinkEntities);

        return new Batch(memberId, bookmark, flowables);
    }

    private List<Flowable> convertToFlowables(List<JournalLinkEntity> journalLinkEntities) {
        List<Flowable> flowables = new ArrayList<>();

        for (JournalLinkEntity journalLinkEntity : journalLinkEntities) {
            flowables.add(new FlowableJournalLinkEvent(journalLinkEntity));
        }
        return flowables;
    }

}
