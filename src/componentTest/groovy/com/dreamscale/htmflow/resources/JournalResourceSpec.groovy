package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.journal.IntentionInputDto
import com.dreamscale.htmflow.api.journal.IntentionOutputDto
import com.dreamscale.htmflow.api.project.ProjectDto
import com.dreamscale.htmflow.api.project.RecentTasksByProjectDto
import com.dreamscale.htmflow.client.JournalClient
import com.dreamscale.htmflow.client.ProjectClient
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
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class JournalResourceSpec extends Specification {

    @Autowired
    JournalClient journalClient

    @Autowired
    ProjectClient projectClient

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
    MasterAccountEntity testUser

    def setup() {
        projectRepository.deleteAll()
        taskRepository.deleteAll()
        intentionRepository.deleteAll()
        organizationRepository.deleteAll()
        organizationMemberRepository.deleteAll()
        recentProjectRepository.deleteAll()
        recentTaskRepository.deleteAll()
    }

    def "should save new intention"() {
        given:
        TaskEntity task = createOrganizationAndTask();
        createMembership(task.getOrganizationId(), testUser.getId());

        IntentionInputDto intentionInputDto = aRandom.intentionInputDto().forTask(task).build();

        when:
        IntentionOutputDto output = journalClient.createIntention(intentionInputDto)

        then:
        assert output != null
        assert output.getId() != null
        assert output.getTaskId() == intentionInputDto.getTaskId()
        assert output.description == intentionInputDto.getDescription()
    }

    def "get recent intentions"() {
        given:
        TaskEntity task = createOrganizationAndTask();
        createMembership(task.getOrganizationId(), testUser.getId());

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task).build();
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task).build();

        journalClient.createIntention(intention1)
        journalClient.createIntention(intention2)

        when:
        List<IntentionOutputDto> intentions = journalClient.getRecentIntentions();

        then:
        assert intentions != null
        assert intentions.size() == 2
    }

    def "get recent projects and tasks"() {
        given:
        OrganizationEntity organization = aRandom.organizationEntity().build()
        organizationRepository.save(organization)

        createMembership(organization.getId(), testUser.getId());

        ProjectEntity project1 = aRandom.projectEntity().forOrg(organization).build()
        projectRepository.save(project1)

        ProjectEntity project2 = aRandom.projectEntity().forOrg(organization).build()
        projectRepository.save(project2)

        TaskEntity task1 = aRandom.taskEntity().forProject(project1).build()
        taskRepository.save(task1)

        TaskEntity task2 = aRandom.taskEntity().forProject(project1).build()
        taskRepository.save(task2)

        TaskEntity task3 = aRandom.taskEntity().forProject(project2).build()
        taskRepository.save(task3)

        TaskEntity task4 = aRandom.taskEntity().forProject(project2).build()
        taskRepository.save(task4)

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task1).build();
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task2).build();
        IntentionInputDto intention3 = aRandom.intentionInputDto().forTask(task3).build();
        IntentionInputDto intention4 = aRandom.intentionInputDto().forTask(task4).build();

        journalClient.createIntention(intention1)
        journalClient.createIntention(intention2)
        journalClient.createIntention(intention3)
        journalClient.createIntention(intention4)

        when:
        RecentTasksByProjectDto recentTasksByProject = projectClient.getRecentTasksByProject();

        then:
        assert recentTasksByProject != null
        assert recentTasksByProject.getRecentProjects().size() == 2

        ProjectDto recentProject1 = recentTasksByProject.getRecentProjects().get(0);
        ProjectDto recentProject2 = recentTasksByProject.getRecentProjects().get(1);

        assert recentTasksByProject.getRecentTasks(recentProject1.getId()).size() == 2;
        assert recentTasksByProject.getRecentTasks(recentProject2.getId()).size() == 2;

    }


    def "get recent intentions for other member"() {
        given:
        TaskEntity task = createOrganizationAndTask();
        OrganizationMemberEntity memberWithIntentions = createMembership(task.getOrganizationId(), testUser.getId());

        IntentionInputDto intention1 = aRandom.intentionInputDto().forTask(task).build();
        IntentionInputDto intention2 = aRandom.intentionInputDto().forTask(task).build();

        journalClient.createIntention(intention1)
        journalClient.createIntention(intention2)

        //change active logged in user to a different user within same organization
        testUser.setId(UUID.randomUUID())
        OrganizationMemberEntity otherMember = createMembership(task.getOrganizationId(), testUser.getId());

        when:
        List<IntentionOutputDto> intentions = journalClient.getRecentIntentionsForMember(memberWithIntentions.getId().toString());

        then:
        assert intentions != null
        assert intentions.size() == 2
    }

    private TaskEntity createOrganizationAndTask() {
        OrganizationEntity organization = aRandom.organizationEntity().build()
        organizationRepository.save(organization)

        ProjectEntity project = aRandom.projectEntity().forOrg(organization).build()
        projectRepository.save(project)

        TaskEntity task = aRandom.taskEntity().forProject(project).build()
        taskRepository.save(task)

        return task
    }

    private OrganizationMemberEntity createMembership(UUID organizationId, UUID masterAccountId) {
        OrganizationMemberEntity member = aRandom.memberEntity()
                .organizationId(organizationId)
                .masterAccountId(masterAccountId)
                .build()
        organizationMemberRepository.save(member)

        return member;
    }

}
