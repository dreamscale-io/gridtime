package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.flow.batch.NewFlowBatchDto
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.client.FlowClient
import com.dreamscale.gridtime.client.JournalClient
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.domain.flow.FlowActivityRepository
import com.dreamscale.gridtime.core.domain.flow.FlowEventRepository
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.capability.active.RecentActivityManager
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
    LearningCircuitClient circuitClient

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
    RootAccountEntity testUser

    @Autowired
    TimeService mockTimeService

    @Autowired
    RecentActivityManager recentActivityService

    @Autowired
    JournalClient journalClient


    OrganizationMemberEntity member

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()

        member = createMemberWithOrgAndTeam()
        testUser.setId(member.getRootAccountId())
    }



    def "publishBatch should save all the things"() {
        given:
        NewFlowBatchDto flowBatch = aRandom.flowBatch().build()

        when:
        flowClient.publishBatch(flowBatch)

        then:
        assert flowActivityRepository.findByMemberId(member.getId()).size() == 5
        assert flowEventRepository.findByMemberId(member.getId()).size() == 1
    }

    def "publishSnippet should save the snippet"() {
        given:
        NewSnippetEventDto snippet = aRandom.snippetEvent().build()

        when:
        LearningCircuitDto circuit = circuitClient.startWTF()
        flowClient.publishSnippet(snippet)

        then:
        assert circuit != null
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
                .rootAccountId(masterAccountId)
                .save()
    }


    private OrganizationMemberEntity createMemberWithOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationEntity org = aRandom.organizationEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(team.id).organizationId(org.id).memberId(member.id).save()

        return member;

    }
}
