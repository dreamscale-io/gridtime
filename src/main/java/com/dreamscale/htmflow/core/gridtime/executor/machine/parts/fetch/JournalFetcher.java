package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch;

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.domain.journal.JournalEntryRepository;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source.Bookmark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JournalFetcher extends FetchStrategy {

    @Autowired
    JournalEntryRepository journalEntryRepository;


    @Override
    public Batch fetchNextBatch(UUID memberId, Bookmark bookmark, int fetchSize) {

        LocalDateTime afterDate = bookmark.getPosition();

        List<JournalEntryEntity> journalEntries =
                journalEntryRepository.findByMemberIdAfterDateWithLimit(memberId, Timestamp.valueOf(afterDate), fetchSize);

        List<Flowable> flowables = convertToFlowables(journalEntries);

        return new Batch(memberId, bookmark, flowables);

    }

    private List<Flowable> convertToFlowables(List<JournalEntryEntity> journalEntries) {
        List<Flowable> flowables = new ArrayList<>();

        for (JournalEntryEntity journalEntryEntity : journalEntries) {
            flowables.add(new FlowableJournalEntry(journalEntryEntity));
        }
        return flowables;
    }

}
