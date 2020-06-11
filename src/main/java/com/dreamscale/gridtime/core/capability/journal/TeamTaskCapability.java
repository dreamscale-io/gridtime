package com.dreamscale.gridtime.core.capability.journal;

import com.dreamscale.gridtime.api.project.CreateTaskInputDto;
import com.dreamscale.gridtime.api.project.TaskDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.capability.external.JiraCapability;
import com.dreamscale.gridtime.core.capability.membership.TeamCapability;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.journal.*;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.dreamscale.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TeamTaskCapability {

    @Autowired
    private GridClock gridClock;

    @Autowired
    private TeamCapability teamCapability;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    TeamTaskRepository teamTaskRepository;

    @Autowired
    private JiraCapability jiraCapability;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<TaskDto, TaskEntity> taskMapper;

    private static final String DEFAULT_TASK_NAME = "No Task";

    @PostConstruct
    private void init() {
        taskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, TaskEntity.class);
    }

    @Transactional
    public TaskDto findOrCreateTeamTask(LocalDateTime now, UUID organizationId, UUID invokingMemberId, UUID projectId, CreateTaskInputDto taskInputDto) {

        TeamDto activeTeam = teamCapability.getMyActiveTeam(organizationId, invokingMemberId);

        String standardizedTaskName = standardizeToLowerCase(taskInputDto.getName());

        TeamTaskEntity teamTask = teamTaskRepository.findByTeamIdAndTeamProjectIdAndLowercaseName(activeTeam.getId(), projectId, standardizedTaskName);

        if (teamTask == null) {
            teamTask = new TeamTaskEntity();
            teamTask.setId(UUID.randomUUID());
            teamTask.setOrganizationId(organizationId);
            teamTask.setCreatorId(invokingMemberId);
            teamTask.setTeamProjectId(projectId);
            teamTask.setTeamId(activeTeam.getId());
            teamTask.setCreationDate(now);
            teamTask.setName(taskInputDto.getName());
            teamTask.setLowercaseName(standardizedTaskName);
            teamTask.setDescription(taskInputDto.getDescription());

            teamTaskRepository.save(teamTask);
        }

        TaskEntity oldTaskEntity = taskRepository.findByProjectIdAndName(projectId, standardizedTaskName);

        if (oldTaskEntity == null) {
            TaskEntity orgTask = new TaskEntity();
            orgTask.setId(teamTask.getId());
            orgTask.setName(teamTask.getName());
            orgTask.setOrganizationId(organizationId);
            orgTask.setProjectId(teamTask.getTeamProjectId());
            orgTask.setSummary(teamTask.getDescription());

            taskRepository.save(orgTask);
        }

        return toDto(teamTask);

    }


    public TaskDto getTeamTask(UUID taskId) {

        TeamTaskEntity teamTask = teamTaskRepository.findOne(taskId);

        if (teamTask != null) {
            return toDto(teamTask);
        }

        TaskEntity oldTask = taskRepository.findOne(taskId);

        if (oldTask != null) {
            return taskMapper.toApi(oldTask);
        }

        return null;
    }


    private TaskDto toDto(TeamTaskEntity taskEntity) {
        TaskDto taskDto = new TaskDto();

        taskDto.setId(taskEntity.getId());
        taskDto.setName(taskEntity.getName());
        taskDto.setSummary(taskEntity.getDescription());
        taskDto.setDescription(taskEntity.getDescription());
        taskDto.setProjectId(taskEntity.getTeamProjectId());

        return taskDto;
    }


    public List<TaskDto> findTasksStartingWith(UUID orgId, UUID projectId, String startsWith) {
        validateProjectWithinOrg(orgId, projectId);

        validateSearchIncludesNumber(startsWith);

        List<TaskEntity> taskEntities = taskRepository.findTop10ByProjectIdAndNameStartingWith(projectId, startsWith);

        return taskMapper.toApiList(taskEntities);
    }

    private void validateSearchIncludesNumber(String startsWith) {
        if (startsWith == null || !startsWith.matches(".*\\d+.*")) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_SEARCH_STRING, "Task search string needs to minimally include a number");
        }
    }

    private void validateProjectWithinOrg(UUID orgId, UUID projectId) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);

        if (projectEntity == null || !projectEntity.getOrganizationId().equals(orgId)) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_PROJECT_REFERENCE, "Project not found in Org");
        }
    }

    private String standardizeToLowerCase(String name) {
        if (name != null) {
            return name.toLowerCase();
        }
        return null;
    }


    public boolean isDefaultTask(TaskDto taskDto) {
        return taskDto != null && DEFAULT_TASK_NAME.equals(taskDto.getName());
    }

    public List<TaskDto> findTasksByRecentMemberAccess(UUID organizationId, UUID memberId, UUID projectId) {

        List<TaskEntity> tasks = taskRepository.findByRecentMemberAccess(memberId, projectId);
        return taskMapper.toApiList(tasks);
    }

    public List<TaskDto> findTasksByRecentTeamAccess(UUID organizationId, UUID teamId, UUID projectId) {

        List<TaskEntity> tasks = taskRepository.findByRecentTeamAccess(teamId, projectId);
        return taskMapper.toApiList(tasks);
    }


    public TaskDto findDefaultProjectTask(UUID organizationId, UUID projectId) {

        TaskEntity defaultTask = taskRepository.findByProjectIdAndName(projectId, DEFAULT_TASK_NAME);

        return taskMapper.toApi(defaultTask);

    }

    public TaskDto findMostRecentTask(UUID organizationId, UUID memberId) {

        TaskEntity activeTask = taskRepository.findMostRecentTaskForMember(memberId);
        return taskMapper.toApi(activeTask);
    }

    @Transactional
    public TaskDto createDefaultProjectTask(LocalDateTime now, UUID organizationId, UUID creatorId, UUID teamId, UUID projectId) {

        TaskEntity defaultTask = new TaskEntity();
        defaultTask.setId(UUID.randomUUID());
        defaultTask.setOrganizationId(organizationId);
        defaultTask.setProjectId(projectId);
        defaultTask.configureDefaultTask();

        taskRepository.save(defaultTask);

        TeamTaskEntity teamTask = new TeamTaskEntity();
        teamTask.setId(UUID.randomUUID());
        teamTask.setOrganizationId(organizationId);
        teamTask.setCreatorId(creatorId);
        teamTask.setTeamProjectId(projectId);
        teamTask.setTeamId(teamId);
        teamTask.setCreationDate(now);
        teamTask.setName(defaultTask.getName());
        teamTask.setLowercaseName(defaultTask.getName().toLowerCase());
        teamTask.setDescription(defaultTask.getSummary());

        teamTaskRepository.save(teamTask);

        return taskMapper.toApi(defaultTask);
    }



}