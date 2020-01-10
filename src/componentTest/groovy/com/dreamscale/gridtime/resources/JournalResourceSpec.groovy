package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.journal.*
import com.dreamscale.gridtime.api.project.ProjectDto
import com.dreamscale.gridtime.api.project.RecentTasksSummaryDto
import com.dreamscale.gridtime.client.CircuitClient
import com.dreamscale.gridtime.client.JournalClient
import com.dreamscale.gridtime.core.domain.active.RecentProjectRepository
import com.dreamscale.gridtime.core.domain.active.RecentTaskRepository
import com.dreamscale.gridtime.core.domain.journal.IntentionRepository
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity
import com.dreamscale.gridtime.core.domain.journal.ProjectRepository
import com.dreamscale.gridtime.core.domain.journal.TaskEntity
import com.dreamscale.gridtime.core.domain.journal.TaskRepository
import com.dreamscale.gridtime.core.domain.member.MasterAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.mapper.DateTimeAPITranslator
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class JournalResourceSpec extends Specification {

    @Autowired
    JournalClient journalClient

    @Autowired
    private CircuitClient circleClient;

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
    MasterAccountEntity loggedInUser

    def "should save new intention"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        createMembership(task.getOrganizationId(), loggedInUser.getId())

        IntentionInputDto intentionInputDto = aRandom.intentionInputDto().forTask(task).build()

        when:
        JournalEntryDto journalEntry = createIntentionWithClient(intentionInputDto)

        then:
        assert journalEntry != null
        assert journalEntry.getId() != null
        assert journalEntry.getTaskId() == intentionInputDto.getTaskId()
        assert journalEntry.description == intentionInputDto.getDescription()
        assert journalEntry.journalEntryType == JournalEntryType.Intention
    }

    def "should update flame rating"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        createMembership(task.getOrganizationId(), loggedInUser.getId())

        IntentionInputDto intentionInputDto = aRandom.intentionInputDto().forTask(task).build()
        JournalEntryDto intention = createIntentionWithClient(intentionInputDto)
        int flameRating = 3;

        when:
        JournalEntryDto result = journalClient.updateRetroFlameRating(intention.getId().toString(), new FlameRatingInputDto(flameRating));

        then:
        assert result != null
        assert result.getId() == intention.getId()
        assert result.getFlameRating() == flameRating;
    }


    def "should finish intention"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        createMembership(task.getOrganizationId(), loggedInUser.getId())

        IntentionInputDto intentionInputDto = aRandom.intentionInputDto().forTask(task).build()
        JournalEntryDto intention = createIntentionWithClient(intentionInputDto)

        1 * mockTimeService.now() >> LocalDateTime.now()
        when:
        JournalEntryDto result = journalClient.finishIntention(intention.getId().toString(), new IntentionFinishInputDto(FinishStatus.done));

        then:
        assert result != null
        assert result.getId() == intention.getId()
        assert result.getFinishStatus() == "done";
    }


    def "get recent intentions"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        createMembership(task.getOrganizationId(), loggedInUser.getId())

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task).build()

        createIntentionWithClient(intention1)
        createIntentionWithClient(intention2)

        when:
        List<JournalEntryDto> intentions = journalClient.getRecentJournal().recentIntentions

        then:
        assert intentions != null
        assert intentions.size() == 2 //task switch event added
    }

    def "get recent intentions with limit"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        createMembership(task.getOrganizationId(), loggedInUser.getId())

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
        createMembership(task.getOrganizationId(), loggedInUser.getId())

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


    def "get recent tasks summary"() {
        given:
        OrganizationEntity organization = aRandom.organizationEntity().save()

        createMembership(organization.getId(), loggedInUser.getId())

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

        RecentTasksSummaryDto recentTasksSummary = journalClient.getRecentTaskReferencesSummary();

        then:
        assert recentTasksSummary != null
        assert recentTasksSummary.getRecentProjects().size() == 2

        ProjectDto recentProject1 = recentTasksSummary.getRecentProjects().get(0)
        ProjectDto recentProject2 = recentTasksSummary.getRecentProjects().get(1)

        assert recentTasksSummary.getRecentTasks(recentProject1.getId()).size() == 2
        assert recentTasksSummary.getRecentTasks(recentProject2.getId()).size() == 2

    }

    def "create a new task reference in the journal"() {
        given:
        OrganizationEntity organization = aRandom.organizationEntity().build()
        organizationRepository.save(organization)

        createMembership(organization.getId(), loggedInUser.getId());

        ProjectEntity project1 = aRandom.projectEntity().forOrg(organization).build()
        projectRepository.save(project1)

        ProjectEntity project2 = aRandom.projectEntity().forOrg(organization).build()
        projectRepository.save(project2)

        TaskEntity task1 = aRandom.taskEntity().forProject(project1).build()
        taskRepository.save(task1)

        TaskEntity task2 = aRandom.taskEntity().forProject(project1).build()
        taskRepository.save(task2)

        TaskReferenceInputDto taskReferenceDto = new TaskReferenceInputDto()

        taskReferenceDto.setTaskName(task1.name)

        when:
        RecentTasksSummaryDto recentTasksSummary = journalClient.createTaskReference(taskReferenceDto);

        then:
        assert recentTasksSummary.getActiveTask() != null

        assert recentTasksSummary != null
        assert recentTasksSummary.getRecentProjects().size() == 2

        ProjectDto recentProject1 = recentTasksSummary.getRecentProjects().get(0);
        ProjectDto recentProject2 = recentTasksSummary.getRecentProjects().get(1);

        assert recentTasksSummary.getRecentTasks(recentProject1.getId()).size() == 2;

    }



    def "get recent intentions for other member"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        OrganizationMemberEntity memberWithIntentions = createMembership(task.getOrganizationId(), loggedInUser.getId())

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task).build()

        createIntentionWithClient(intention1)
        createIntentionWithClient(intention2)

        //change active logged in user to a different user within same organization
        loggedInUser.setId(UUID.randomUUID())
        OrganizationMemberEntity otherMember = createMembership(task.getOrganizationId(), loggedInUser.getId())

        when:
        List<JournalEntryDto> intentions = journalClient.getRecentJournalForMember(memberWithIntentions.getId().toString()).recentIntentions

        then:
        assert intentions != null
        assert intentions.size() == 2
    }

    def "get recent intentions for other member with limit"() {
        given:
        TaskEntity task = createOrganizationAndTask()
        OrganizationMemberEntity memberWithIntentions = createMembership(task.getOrganizationId(), loggedInUser.getId())

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task).build()
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task).build()

        createIntentionWithClient(intention1)
        createIntentionWithClient(intention2)

        //change active logged in user to a different user within same organization
        loggedInUser.setId(UUID.randomUUID())
        OrganizationMemberEntity otherMember = createMembership(task.getOrganizationId(), loggedInUser.getId())

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
