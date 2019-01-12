package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.batch.NewFlowBatch
import com.dreamscale.htmflow.api.event.NewSnippetEvent
import com.dreamscale.htmflow.client.FlowClient
import com.dreamscale.htmflow.core.domain.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.OrganizationEntity
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity
import com.dreamscale.htmflow.core.domain.OrganizationMemberRepository
import com.dreamscale.htmflow.core.domain.OrganizationRepository
import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity
import com.dreamscale.htmflow.core.domain.flow.FlowActivityRepository
import com.dreamscale.htmflow.core.domain.flow.FlowEventRepository
import com.dreamscale.htmflow.core.service.TimeService
import org.dreamscale.exception.ForbiddenException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class FlowResourceSpec extends Specification {

    @Autowired
    FlowClient flowClient

    @Autowired
    FlowClient unauthenticatedFlowClient

    @Autowired
    FlowActivityRepository flowActivityRepository

    @Autowired
    FlowEventRepository flowEventRepository

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    OrganizationMemberRepository organizationMemberRepository

    @Autowired
    MasterAccountEntity testUser

    @Autowired
    TimeService mockTimeService

    OrganizationEntity org
    OrganizationMemberEntity member

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()

        org = aRandom.organizationEntity().save()
        member = createMembership(org.getId(), testUser.getId())
    }

    def "addBatch should assign component to all the saved things"() {
        given:
        NewFlowBatch flowBatch = aRandom.flowBatch().build()

        when:
        flowClient.addBatch(flowBatch)

        List<FlowActivityEntity> activities = flowActivityRepository.findByMemberId(member.getId())

        then:
        assert activities.size() == 5
        assert activities.get(0).component != null

    }

    def "addBatch should save all the things"() {
        given:
        NewFlowBatch flowBatch = aRandom.flowBatch().build()

        when:
        flowClient.addBatch(flowBatch)

        then:
        assert flowActivityRepository.findByMemberId(member.getId()).size() == 5
        assert flowEventRepository.findByMemberId(member.getId()).size() == 1
    }

    def "addSnippet should save the snippet"() {
        given:
        NewSnippetEvent snippet = aRandom.snippetEvent().build()

        when:
        flowClient.addSnippet(snippet)

        then:
        assert flowActivityRepository.findByMemberId(member.getId()).size() == 0
        assert flowEventRepository.findByMemberId(member.getId()).size() == 1
    }

    def "authPing should throw ForbiddenException if not authorized"() {
        when:
        flowClient.authPing()
        
        then:
        notThrown(Exception)

        when:
        unauthenticatedFlowClient.authPing()

        then:
        thrown(ForbiddenException)
    }


    private OrganizationMemberEntity createMembership(UUID organizationId, UUID masterAccountId) {
        aRandom.memberEntity()
                .organizationId(organizationId)
                .masterAccountId(masterAccountId)
                .save()
    }

}
