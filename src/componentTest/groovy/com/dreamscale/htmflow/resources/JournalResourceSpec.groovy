package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.journal.ChunkEventInputDto
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto
import com.dreamscale.htmflow.api.project.ProjectDto
import com.dreamscale.htmflow.api.project.RecentTasksByProjectDto
import com.dreamscale.htmflow.client.JournalClient
import com.dreamscale.htmflow.client.ProjectClient
import com.dreamscale.htmflow.core.domain.ChunkEventRepository
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
    ChunkEventRepository chunkEventRepository

    @Autowired
    RecentProjectRepository recentProjectRepository

    @Autowired
    RecentTaskRepository recentTaskRepository

    @Autowired
    MasterAccountEntity testUser

    def setup() {
        projectRepository.deleteAll()
        taskRepository.deleteAll()
        chunkEventRepository.deleteAll()
        organizationRepository.deleteAll()
        organizationMemberRepository.deleteAll()
        recentProjectRepository.deleteAll()
        recentTaskRepository.deleteAll()
    }

    def "should save new chunk"() {
        given:
        TaskEntity task = createOrganizationAndTask();
        createMembership(task.getOrganizationId(), testUser.getId());

        ChunkEventInputDto chunkEvent = aRandom.chunkEventInputDto().forTask(task).build();

        when:
        ChunkEventOutputDto output = journalClient.createChunkEvent(chunkEvent)

        then:
        assert output != null
        assert output.getId() != null
        assert output.getTaskId() == chunkEvent.getTaskId()
        assert output.description == chunkEvent.getDescription()
    }

    def "get recent chunks"() {
        given:
        TaskEntity task = createOrganizationAndTask();
        createMembership(task.getOrganizationId(), testUser.getId());

        ChunkEventInputDto chunkEvent1 = aRandom.chunkEventInputDto().forTask(task).build();
        ChunkEventInputDto chunkEvent2 = aRandom.chunkEventInputDto().forTask(task).build();

        journalClient.createChunkEvent(chunkEvent1)
        journalClient.createChunkEvent(chunkEvent2)

        when:
        List<ChunkEventOutputDto>  chunks = journalClient.getRecentChunks();

        then:
        assert chunks != null
        assert chunks.size() == 2
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

        ChunkEventInputDto chunkEvent1 = aRandom.chunkEventInputDto().forTask(task1).build();
        ChunkEventInputDto chunkEvent2 = aRandom.chunkEventInputDto().forTask(task2).build();
        ChunkEventInputDto chunkEvent3 = aRandom.chunkEventInputDto().forTask(task3).build();
        ChunkEventInputDto chunkEvent4 = aRandom.chunkEventInputDto().forTask(task4).build();

        journalClient.createChunkEvent(chunkEvent1)
        journalClient.createChunkEvent(chunkEvent2)
        journalClient.createChunkEvent(chunkEvent3)
        journalClient.createChunkEvent(chunkEvent4)

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


    def "get recent chunks for other member"() {
        given:
        TaskEntity task = createOrganizationAndTask();
        OrganizationMemberEntity memberWithChunks = createMembership(task.getOrganizationId(), testUser.getId());

        ChunkEventInputDto chunkEvent1 = aRandom.chunkEventInputDto().forTask(task).build();
        ChunkEventInputDto chunkEvent2 = aRandom.chunkEventInputDto().forTask(task).build();

        journalClient.createChunkEvent(chunkEvent1)
        journalClient.createChunkEvent(chunkEvent2)

        //change active user to a different user
        testUser.setId(UUID.randomUUID())
        OrganizationMemberEntity otherMember = createMembership(task.getOrganizationId(), testUser.getId());

        when:
        List<ChunkEventOutputDto>  chunks = journalClient.getRecentChunksForMember(memberWithChunks.getId().toString());

        then:
        assert chunks != null
        assert chunks.size() == 2
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
