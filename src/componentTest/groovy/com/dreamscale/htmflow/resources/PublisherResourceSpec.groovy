package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.batch.NewBatchEvent
import com.dreamscale.htmflow.api.batch.NewIFMBatch
import com.dreamscale.htmflow.api.event.EventType
import com.dreamscale.htmflow.client.FlowClient
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
class PublisherResourceSpec extends Specification {

    @Autowired
    FlowClient publisherClient

    def "addIfmBatch should not explode"() {
        given:
        NewIFMBatch batch = NewIFMBatch.builder()
                .timeSent(LocalDateTime.now())
                .event(NewBatchEvent.builder().comment("some text").type(EventType.ACTIVATE).build())
                .build()

        when:
        publisherClient.addIFMBatch(batch)

        then:
        notThrown(Exception.class)
    }

}
