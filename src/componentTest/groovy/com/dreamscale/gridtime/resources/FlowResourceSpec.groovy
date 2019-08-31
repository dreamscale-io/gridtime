package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.batch.NewFlowBatch
import com.dreamscale.gridtime.api.event.NewSnippetEvent
import com.dreamscale.gridtime.client.FlowClient
import com.dreamscale.gridtime.client.JournalClient
import com.dreamscale.gridtime.core.domain.member.MasterAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.domain.flow.FlowActivityRepository
import com.dreamscale.gridtime.core.domain.flow.FlowEventRepository
import com.dreamscale.gridtime.core.service.RecentActivityService
import com.dreamscale.gridtime.core.service.TimeService
import org.dreamscale.exception.ForbiddenException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

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

    @Autowired
    RecentActivityService recentActivityService

    @Autowired
    JournalClient journalClient


    OrganizationEntity org
    OrganizationMemberEntity member

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()

        org = aRandom.organizationEntity().save()
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