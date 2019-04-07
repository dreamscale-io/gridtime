package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.domain.member.json.LinkedMember;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.details.AuthorDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandLayerType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * When a person is pairing, generates multiple author references for the frame as a TimeBand,
 * otherwise generates a single author.  Attribution of authors is by Intention
 */
@Slf4j
public class JournalAuthorObserver implements FlowObserver {

    @Override
    public void see(StoryTile currentStoryTile, Window window) {

        List<Flowable> flowables = window.getFlowables();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableJournalEntry) {
                JournalEntryEntity journalEntry = ((JournalEntryEntity) flowable.get());

                if (journalEntry.getLinked() != null && journalEntry.getLinked()) {
                    List<LinkedMember> linkedMembers = journalEntry.getLinkedMembers();

                    currentStoryTile.startBand(BandLayerType.PAIRING_AUTHORS, journalEntry.getPosition(), new AuthorDetails(linkedMembers));
                } else {
                    currentStoryTile.clearBand(BandLayerType.PAIRING_AUTHORS, journalEntry.getPosition());
                }
            }
        }

        currentStoryTile.finishAfterLoad();


    }


}
