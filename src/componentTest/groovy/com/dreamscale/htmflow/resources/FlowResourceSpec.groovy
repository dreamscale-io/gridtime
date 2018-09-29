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
import com.dreamscale.htmflow.core.domain.flow.FlowEventEntity
import com.dreamscale.htmflow.core.domain.flow.FlowEventRepository
import com.dreamscale.htmflow.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class FlowResourceSpec extends Specification {

    @Autowired
    FlowClient flowClient

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
        flowActivityRepository.deleteAll()
        flowEventRepository.deleteAll()
        organizationRepository.deleteAll()
        organizationMemberRepository.deleteAll()

        mockTimeService.now() >> LocalDateTime.now()

        org = createOrganization()
        member = createMembership(org.getId(), testUser.getId())
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


    private OrganizationEntity createOrganization() {
        OrganizationEntity organization = aRandom.organizationEntity().build()
        organizationRepository.save(organization)

        return organization
    }

    private OrganizationMemberEntity createMembership(UUID organizationId, UUID masterAccountId) {
        OrganizationMemberEntity member = aRandom.memberEntity()
                .organizationId(organizationId)
                .masterAccountId(masterAccountId)
                .build()
        organizationMemberRepository.save(member)

        return member
    }

}
