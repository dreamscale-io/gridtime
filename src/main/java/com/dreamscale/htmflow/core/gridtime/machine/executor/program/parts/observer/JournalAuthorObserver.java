package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer;

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source.Window;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.AuthorsDetails;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * When a person is pairing, generates multiple author references for the frame as a TimeBand,
 * otherwise generates a single author.  Attribution of authors is by Intention
 */
@Slf4j
public class JournalAuthorObserver implements FlowObserver<FlowableJournalEntry> {

    @Override
    public void see(Window<FlowableJournalEntry> window, GridTile gridTile) {

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
