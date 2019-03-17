package com.dreamscale.htmflow.core.feeds.executor.parts.fetch

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Bookmark
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class FileActivityFetcherSpec extends Specification{

    @Autowired
    FileActivityFetcher fileActivityFetcher

    def "should fetch batches based on time and sequence"() {
        given:
        UUID memberId = UUID.randomUUID();
        LocalDateTime time1 = aRandom.localDateTime()
        LocalDateTime time2 = time1.plusMinutes(20);
        LocalDateTime time3 = time2.plusMinutes(20);
        LocalDateTime time4 = time3.plusMinutes(20);

        FlowActivityEntity flowActivity1 = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(time1)
                .end(time2)
                .save()

        FlowActivityEntity flowActivity2 = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(time2)
                .end(time3)
                .save()

        FlowActivityEntity flowActivity3 = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(time3)
                .end(time4)
                .save()
        when:
        Batch batch = fileActivityFetcher.fetchNextBatch(memberId, new Bookmark(time1), 100)

        then:
        assert batch.flowables.size() == 2
        assert batch.flowables.get(0).id == flowActivity2.id
        assert batch.flowables.get(1).id == flowActivity3.id
    }
}
