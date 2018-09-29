package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.account.*
import com.dreamscale.htmflow.api.batch.NewIFMBatch
import com.dreamscale.htmflow.api.organization.MemberRegistrationDetailsDto
import com.dreamscale.htmflow.api.organization.MembershipInputDto
import com.dreamscale.htmflow.api.organization.OrganizationDto
import com.dreamscale.htmflow.api.organization.OrganizationInputDto
import com.dreamscale.htmflow.api.status.ConnectionResultDto
import com.dreamscale.htmflow.api.status.Status
import com.dreamscale.htmflow.client.FlowClient
import com.dreamscale.htmflow.core.domain.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.OrganizationEntity
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity
import com.dreamscale.htmflow.core.domain.OrganizationMemberRepository
import com.dreamscale.htmflow.core.domain.OrganizationRepository
import com.dreamscale.htmflow.core.domain.ProjectEntity
import com.dreamscale.htmflow.core.domain.TaskEntity
import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity
import com.dreamscale.htmflow.core.domain.flow.FlowActivityRepository
import com.dreamscale.htmflow.core.domain.flow.FlowEventEntity
import com.dreamscale.htmflow.core.domain.flow.FlowEventRepository
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraUserDto
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

    def setup() {
        flowActivityRepository.deleteAll()
        flowEventRepository.deleteAll()
        organizationRepository.deleteAll()
        organizationMemberRepository.deleteAll()

        mockTimeService.now() >> LocalDateTime.now()
    }

    def "should save all the things"() {
        given:
        OrganizationEntity org = createOrganization()
        OrganizationMemberEntity member = createMembership(org.getId(), testUser.getId())

        NewIFMBatch flowBatch = aRandom.flowBatch().build()

        when:
        flowClient.addIFMBatch(flowBatch)

        List<FlowActivityEntity> activities = flowActivityRepository.findByMemberId(member.getId())
        List<FlowEventEntity> events = flowEventRepository.findByMemberId(member.getId())

        then:
        assert activities != null
        assert activities.size() == 5

        assert events != null
        assert events.size() == 2

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
