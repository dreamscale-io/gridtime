package com.dreamscale.gridtime.core.machine.executor.program.parts.feed

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity
import com.dreamscale.gridtime.core.domain.journal.TaskEntity
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Bookmark
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class JournalFeedStrategySpec extends Specification{

    @Autowired
    JournalFeedStrategy journalFetcher

    def "should fetch batches based on time after"() {
        given:
        UUID memberId = UUID.randomUUID();
        LocalDateTime time1 = aRandom.localDateTime()
        LocalDateTime time2 = time1.plusMinutes(20);
        LocalDateTime time3 = time2.plusMinutes(20);

        ProjectEntity projectEntity = aRandom.projectEntity().save();
        TaskEntity taskEntity = aRandom.taskEntity().forProject(projectEntity).save();


        IntentionEntity journalEntry1 = aRandom.intentionEntity()
                .memberId(memberId)
                .position(time1)
                .projectId(projectEntity.id)
                .taskId(taskEntity.id)
                .save()

        IntentionEntity journalEntry2 = aRandom.intentionEntity()
                .memberId(memberId)
                .position(time2)
                .projectId(projectEntity.id)
                .taskId(taskEntity.id)
                .save()

        IntentionEntity journalEntry3 = aRandom.intentionEntity()
                .memberId(memberId)
                .position(time3)
                .projectId(projectEntity.id)
                .taskId(taskEntity.id)
                .save()

        when:
        Batch batch = journalFetcher.fetchNextBatch(memberId, new Bookmark(time1), 100)

        then:
        assert batch.flowables.size() == 3
        assert batch.flowables.get(0).get().id == journalEntry1.id
        assert batch.flowables.get(1).get().id == journalEntry2.id
        assert batch.flowables.get(2).get().id == journalEntry3.id
    }
}
