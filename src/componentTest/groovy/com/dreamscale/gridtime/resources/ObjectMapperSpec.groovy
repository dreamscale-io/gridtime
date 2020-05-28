package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.journal.*
import com.dreamscale.gridtime.api.project.ProjectDto
import com.dreamscale.gridtime.api.project.RecentTasksSummaryDto
import com.dreamscale.gridtime.client.JournalClient
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.core.domain.active.RecentProjectRepository
import com.dreamscale.gridtime.core.domain.active.RecentTaskRepository
import com.dreamscale.gridtime.core.domain.journal.*
import com.dreamscale.gridtime.core.domain.member.*
import com.dreamscale.gridtime.core.mapper.DateTimeAPITranslator
import com.dreamscale.gridtime.core.service.GridClock
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class ObjectMapperSpec extends Specification {

    @Autowired
    ObjectMapper mapper

    def "should test mapper"() {
        given:

        LocalDateTime now = LocalDateTime.now()

        when:
        String value = mapper.writer().writeValueAsString(now);

        println value

        then:
        assert value != null
    }

}
