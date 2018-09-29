package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.journal.IntentionInputDto
import com.dreamscale.htmflow.api.journal.IntentionDto
import com.dreamscale.htmflow.api.journal.JournalEntryDto
import com.dreamscale.htmflow.api.project.ProjectDto
import com.dreamscale.htmflow.api.project.RecentTasksByProjectDto
import com.dreamscale.htmflow.client.JournalClient
import com.dreamscale.htmflow.core.domain.IntentionRepository
import com.dreamscale.htmflow.core.domain.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.OrganizationEntity
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity
import com.dreamscale.htmflow.core.domain.OrganizationMemberRepository
import com.dreamscale.htmflow.core.domain.OrganizationRepository
import com.dreamscale.htmflow.core.domain.ProjectEntity
import com.dreamscale.htmflow.core.domain.ProjectRepository
import com.dreamscale.htmflow.core.domain.RecentProjectRepository
import com.dreamscale.htmflow.core.domain.RecentTaskRepository
import com.dreamscale.htmflow.core.domain.TaskEntity
import com.dreamscale.htmflow.core.domain.TaskRepository
import com.dreamscale.htmflow.core.mapper.DateTimeAPITranslator
import com.dreamscale.htmflow.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class JournalResourceSpec extends Specification {

    @Autowired
    JournalClient journalClient

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    OrganizationMemberRepository organizationMemberRepository

    @Autowired
    ProjectRepository projectRepository
    @Autowired
    TaskRepository taskRepository
    @Autowired
    IntentionRepository intentionRepository

    @Autowired
    RecentProjectRepository recentProjectRepository

    @Autowired
    RecentTaskRepository recentTaskRepository

    @Autowired
    TimeService mockTimeService

    @Autowired
    MasterAccountEntity testUser

    def "should save new intention"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        createMembership(task.getOrganizationId(), testUser.getId())

        IntentionInputDto intentionInputDto = aRandom.intentionInputDto().forTask(task).build()

        when:
        JournalEntryDto intention = createIntentionWithClient(intentionInputDto)

        then:
        assert intention != null
        assert intention.getId() != null
        assert intention.getTaskId() == intentionInputDto.getTaskId()
        assert intention.description == intentionInputDto.getDescription()
    }

    def "get recent intentions"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        createMembership(task.getOrganizationId(), testUser.getId())

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task).build()

        createIntentionWithClient(intention1)
        createIntentionWithClient(intention2)

        when:
        List<JournalEntryDto> intentions = journalClient.getRecentJournal().recentIntentions

        then:
        assert intentions != null
        assert intentions.size() == 2
    }

    def "get recent intentions with limit"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        createMembership(task.getOrganizationId(), testUser.getId())

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention3 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention4 = aRandom.intentionInputDto().forTask(task).build()


        createIntentionWithClient(intention1)
        createIntentionWithClient(intention2)
        createIntentionWithClient(intention3)
        createIntentionWithClient(intention4)

        when:
        List<JournalEntryDto> intentions = journalClient.getRecentJournalWithLimit(3).recentIntentions

        then:
        assert intentions != null
        assert intentions.size() == 3
    }

    def "get historical intentions before date"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        createMembership(task.getOrganizationId(), testUser.getId())

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention3 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention4 = aRandom.intentionInputDto().forTask(task).build()

        3 * mockTimeService.now() >> LocalDateTime.now().minusDays(5)

        journalClient.createIntention(intention1)
        journalClient.createIntention(intention2)
        journalClient.createIntention(intention3)

        1 * mockTimeService.now() >> LocalDateTime.now()

        journalClient.createIntention(intention4)

        String beforeDateStr = DateTimeAPITranslator.convertToString(LocalDateTime.now().minusDays(1))

        when:
        List<JournalEntryDto> intentions = journalClient.getHistoricalIntentionsWithLimit(beforeDateStr, 5)

        then:
        assert intentions != null
        assert intentions.size() == 3
    }


    def "get recent projects and tasks"() {
        given:
        OrganizationEntity organization = aRandom.organizationEntity().save()

        createMembership(organization.getId(), testUser.getId())

        ProjectEntity project1 = aRandom.projectEntity().forOrg(organization).save()
        ProjectEntity project2 = aRandom.projectEntity().forOrg(organization).save()

        TaskEntity task1 = aRandom.taskEntity().forProject(project1).save()
        TaskEntity task2 = aRandom.taskEntity().forProject(project1).save()
        TaskEntity task3 = aRandom.taskEntity().forProject(project2).save()
        TaskEntity task4 = aRandom.taskEntity().forProject(project2).save()

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task1).build()
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task2).build()
        IntentionInputDto intention3 = aRandom.intentionInputDto().forTask(task3).build()
        IntentionInputDto intention4 = aRandom.intentionInputDto().forTask(task4).build()

        createIntentionWithClient(intention1)
        createIntentionWithClient(intention2)
        createIntentionWithClient(intention3)
        createIntentionWithClient(intention4)

        when:
        RecentTasksByProjectDto recentTasksByProject = journalClient.getRecentTasksByProject()

        then:
        assert recentTasksByProject != null
        assert recentTasksByProject.getRecentProjects().size() == 2

        ProjectDto recentProject1 = recentTasksByProject.getRecentProjects().get(0)
        ProjectDto recentProject2 = recentTasksByProject.getRecentProjects().get(1)

        assert recentTasksByProject.getRecentTasks(recentProject1.getId()).size() == 2
        assert recentTasksByProject.getRecentTasks(recentProject2.getId()).size() == 2

    }


    def "get recent intentions for other member"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        OrganizationMemberEntity memberWithIntentions = createMembership(task.getOrganizationId(), testUser.getId())

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task).build()

        createIntentionWithClient(intention1)
        createIntentionWithClient(intention2)

        //change active logged in user to a different user within same organization
        testUser.setId(UUID.randomUUID())
        OrganizationMemberEntity otherMember = createMembership(task.getOrganizationId(), testUser.getId())

        when:
        List<JournalEntryDto> intentions = journalClient.getRecentJournalForMember(memberWithIntentions.getId().toString()).recentIntentions

        then:
        assert intentions != null
        assert intentions.size() == 2
    }

    def "get recent intentions for other member with limit"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        OrganizationMemberEntity memberWithIntentions = createMembership(task.getOrganizationId(), testUser.getId())

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task).build()

        createIntentionWithClient(intention1)
        createIntentionWithClient(intention2)

        //change active logged in user to a different user within same organization
        testUser.setId(UUID.randomUUID())
        OrganizationMemberEntity otherMember = createMembership(task.getOrganizationId(), testUser.getId())

        when:
        List<JournalEntryDto> intentions = journalClient.getRecentJournalForMemberWithLimit(
                memberWithIntentions.getId().toString(), 1).recentIntentions

        then:
        assert intentions != null
        assert intentions.size() == 1
    }

    private JournalEntryDto createIntentionWithClient(IntentionInputDto intentionInputDto) {
        1 * mockTimeService.now() >> LocalDateTime.now()
        return journalClient.createIntention(intentionInputDto)
    }

    private TaskEntity createOrganizationAndTask() {
        OrganizationEntity organization = aRandom.organizationEntity().save()

        ProjectEntity project = aRandom.projectEntity().forOrg(organization).save()

        TaskEntity task = aRandom.taskEntity().forProject(project).save()

        return task
    }

    private OrganizationMemberEntity createMembership(UUID organizationId, UUID masterAccountId) {
        OrganizationMemberEntity member = aRandom.memberEntity()
                .organizationId(organizationId)
                .masterAccountId(masterAccountId)
                .save()

        return member
    }

}
