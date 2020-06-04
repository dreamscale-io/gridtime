package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

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
