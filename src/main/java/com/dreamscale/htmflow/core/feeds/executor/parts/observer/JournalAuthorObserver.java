package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.domain.member.MemberNameEntity;
import com.dreamscale.htmflow.core.domain.member.MemberNameRepository;
import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.details.AuthorDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * When a person is pairing, generates multiple author references for the frame as a TimeBand,
 * otherwise generates a single author.  Attribution of authors is by Intention
 */
@Slf4j
@Component
public class JournalAuthorObserver implements FlowObserver {

    @Autowired
    MemberNameRepository memberNameRepository;

    @Override
    public void see(Window window, StoryTile currentStoryTile) {

        List<Flowable> flowables = window.getFlowables();

        Member me = lookupMe(flowables);

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableJournalEntry) {
                JournalEntryEntity journalEntry = ((JournalEntryEntity) flowable.get());

                if (journalEntry.getLinked() != null && journalEntry.getLinked()) {
                    List<Member> linkedMembers = journalEntry.getLinkedMembers();

                    currentStoryTile.startAuthorsBand(journalEntry.getPosition(), new AuthorDetails(linkedMembers));
                } else {
                    currentStoryTile.startAuthorsBand(journalEntry.getPosition(), new AuthorDetails(me));
                }
            }
        }

        currentStoryTile.finishAfterLoad();

    }

    private Member lookupMe(List<Flowable> flowables) {
        Member me = null;

        if (flowables.size() > 0) {
            JournalEntryEntity journalEntry = ((JournalEntryEntity) flowables.get(0).get());
            MemberNameEntity memberNameEntity = memberNameRepository.findByTorchieId(journalEntry.getMemberId());

            me = new Member(memberNameEntity.getTorchieId().toString(), memberNameEntity.getFullName());
        }
        return me;
    }


}
