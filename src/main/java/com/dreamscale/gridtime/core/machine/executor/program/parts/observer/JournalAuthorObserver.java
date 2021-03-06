package com.dreamscale.gridtime.core.machine.executor.program.parts.observer;

import com.dreamscale.gridtime.core.domain.journal.JournalEntryEntity;
import com.dreamscale.gridtime.core.domain.member.json.Member;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window;
import com.dreamscale.gridtime.core.machine.memory.feature.details.AuthorsDetails;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableJournalEntry;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * When a person is pairing, generates multiple author references for the frame as a TimeBand,
 * otherwise generates a single author.  Attribution of authors is by Intention
 */
@Slf4j
public class JournalAuthorObserver implements FlowObserver<FlowableJournalEntry> {

    @Override
    public void observe(Window<FlowableJournalEntry> window, GridTile gridTile) {

        for (Flowable flowable : window.getFlowables()) {
            JournalEntryEntity journalEntry = (flowable.get());

            if (journalEntry.getLinked() != null && journalEntry.getLinked()) {
                List<Member> linkedMembers = journalEntry.getLinkedMembers();

                gridTile.startAuthors(journalEntry.getPosition(), new AuthorsDetails(linkedMembers));
            } else {
                gridTile.clearAuthors(journalEntry.getPosition());
            }
        }
    }


}
