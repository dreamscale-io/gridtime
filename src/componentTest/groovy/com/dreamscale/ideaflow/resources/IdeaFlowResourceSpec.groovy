package com.dreamscale.ideaflow.resources

import com.dreamscale.ideaflow.ComponentTest
import com.dreamscale.ideaflow.api.activity.NewEditorActivity
import com.dreamscale.ideaflow.api.batch.NewFlowBatch
import com.dreamscale.ideaflow.api.event.NewSnippetEvent
import com.dreamscale.ideaflow.api.journal.IntentionInputDto
import com.dreamscale.ideaflow.client.FlowClient
import com.dreamscale.ideaflow.client.JournalClient
import com.dreamscale.ideaflow.core.domain.MasterAccountEntity
import com.dreamscale.ideaflow.core.domain.OrganizationEntity
import com.dreamscale.ideaflow.core.domain.OrganizationMemberEntity
import com.dreamscale.ideaflow.core.domain.OrganizationMemberRepository
import com.dreamscale.ideaflow.core.domain.OrganizationRepository
import com.dreamscale.ideaflow.core.domain.ProjectEntity
import com.dreamscale.ideaflow.core.domain.flow.FlowActivityEntity
import com.dreamscale.ideaflow.core.domain.flow.FlowActivityRepository
import com.dreamscale.ideaflow.core.domain.flow.FlowEventRepository
import com.dreamscale.ideaflow.core.service.ComponentLookupService
import com.dreamscale.ideaflow.core.service.RecentActivityService
import com.dreamscale.ideaflow.core.service.TimeService
import org.dreamscale.exception.ForbiddenException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom

@ComponentTest
class IdeaFlowResourceSpec extends Specification {

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
    ComponentLookupService componentLookupService

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



    def "should assign CUSTOM component to matching saved things"() {

        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())
        ProjectEntity project = aRandom.projectEntity().organizationId(org.id).save()
        IntentionInputDto intention = aRandom.intentionInputDto().projectId(project.id).build()
        journalClient.createIntention(intention)

        componentLookupService.configureMapping(intention.getProjectId(), "componentA", "/src/comp/*")

        NewEditorActivity editorActivity = aRandom.editorActivity().filePath("/src/comp/Hello.java").build()
        NewFlowBatch flowBatch = aRandom.flowBatch().editorActivity(editorActivity).build()

        when:
        flowClient.addBatch(flowBatch)

        List<FlowActivityEntity> activities = flowActivityRepository.findByMemberId(member.getId())

        then:
        boolean hasMapping = false;
        for (FlowActivityEntity activity : activities) {
            println activity.getComponent() + " | " + activity.getActivityType()
            if ( activity.getComponent() != "default") {
                assert activity.getComponent() == "componentA"
                hasMapping = true
            }
        }
        assert hasMapping
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