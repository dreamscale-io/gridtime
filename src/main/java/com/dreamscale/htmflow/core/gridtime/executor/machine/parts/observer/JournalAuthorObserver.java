package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer;

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.AuthorsDetails;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * When a person is pairing, generates multiple author references for the frame as a TimeBand,
 * otherwise generates a single author.  Attribution of authors is by Intention
 */
@Slf4j
@Component
public class JournalAuthorObserver implements FlowObserver<FlowableJournalEntry> {

    @Override
    public void seeInto(List<FlowableJournalEntry> flowables, GridTile gridTile) {


        for (Flowable flowable : flowables) {
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
