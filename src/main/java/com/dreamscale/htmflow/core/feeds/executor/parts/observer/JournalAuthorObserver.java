package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.domain.member.MemberNameEntity;
import com.dreamscale.htmflow.core.domain.member.MemberNameRepository;
import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;
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
public class JournalAuthorObserver implements FlowObserver<FlowableJournalEntry> {

    @Autowired
    MemberNameRepository memberNameRepository;

    @Override
    public void seeInto(List<FlowableJournalEntry> flowables, TileBuilder tileBuilder) {

        Member me = lookupMe(flowables);

        for (Flowable flowable : flowables) {
            JournalEntryEntity journalEntry = (flowable.get());

            if (journalEntry.getLinked() != null && journalEntry.getLinked()) {
                List<Member> linkedMembers = journalEntry.getLinkedMembers();

                tileBuilder.startAuthorsBand(journalEntry.getPosition(), new AuthorDetails(linkedMembers));
            } else {
                tileBuilder.startAuthorsBand(journalEntry.getPosition(), new AuthorDetails(me));
            }
        }

        tileBuilder.finishAfterLoad();

    }

    private Member lookupMe(List<? extends Flowable> flowables) {
        Member me = null;

        if (flowables.size() > 0) {
            JournalEntryEntity journalEntry = (flowables.get(0).get());
            MemberNameEntity memberNameEntity = memberNameRepository.findByTorchieId(journalEntry.getMemberId());

            me = new Member(memberNameEntity.getTorchieId().toString(), memberNameEntity.getFullName());
        }
        return me;
    }


}
