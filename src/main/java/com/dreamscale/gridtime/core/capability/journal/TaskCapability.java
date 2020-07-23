package com.dreamscale.gridtime.core.capability.journal;

import com.dreamscale.gridtime.api.project.CreateTaskInputDto;
import com.dreamscale.gridtime.api.project.TaskDto;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.journal.*;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TaskCapability {

    @Autowired
    private GridClock gridClock;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PrivateTaskRepository privateTaskRepository;

    @Autowired
    private RecentAllTaskRepository recentAllTaskRepository;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<TaskDto, TaskEntity> taskMapper;
    private DtoEntityMapper<TaskDto, PrivateTaskEntity> privateTaskMapper;
    private DtoEntityMapper<TaskDto, RecentAllTaskEntity> recentAllTaskMapper;

    @PostConstruct
    private void init() {
        taskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, TaskEntity.class);
        privateTaskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, PrivateTaskEntity.class);
        recentAllTaskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, RecentAllTaskEntity.class);
    }

    @Transactional
    public TaskDto findOrCreateTask(UUID organizationId, UUID invokingMemberId, UUID projectId, CreateTaskInputDto taskInputDto) {

        String standardizedTaskName = standardizeToLowerCase(taskInputDto.getName());

        //okay now I've got this new input context, where the project I'm in, could be a public project, and then input for the task is private
        //in all other cases, the normal private tasks within private projects, are in the task table.

        ProjectEntity project = projectRepository.findByOrganizationIdAndId(organizationId, projectId);


        validateProjectFound(projectId.toString(), project);

        TaskDto taskDto = null;

        if (project.isDefault() || (project.isPublic() && taskInputDto.isPrivate())) {
            taskDto = findOrCreatePrivateTask(organizationId, invokingMemberId, projectId, taskInputDto, standardizedTaskName);
        } else {
            taskDto = findOrCreateNormalTask(organizationId, projectId, project.isPrivate(), taskInputDto, standardizedTaskName);
        }

       return taskDto;
    }

    private TaskDto findOrCreateNormalTask(UUID organizationId, UUID projectId, boolean isPrivate, CreateTaskInputDto taskInputDto, String standardizedTaskName) {

        TaskEntity task = taskRepository.findByOrganizationIdAndProjectIdAndLowercaseName(organizationId, projectId, standardizedTaskName);

        if (task == null) {
            task = new TaskEntity();
            task.setId(UUID.randomUUID());
            task.setOrganizationId(organizationId);
            task.setProjectId(projectId);
            task.setName(taskInputDto.getName());
            task.setLowercaseName(standardizedTaskName);
            task.setDescription(taskInputDto.getDescription());

            taskRepository.save(task);
        }

        return toDto(isPrivate, task);
    }

    private TaskDto toDto(boolean isPrivate, TaskEntity task) {
        TaskDto taskDto = taskMapper.toApi(task);
        taskDto.setPrivate(isPrivate);

        return taskDto;
    }

    private TaskDto findOrCreatePrivateTask(UUID organizationId, UUID invokingMemberId, UUID projectId, CreateTaskInputDto taskInputDto, String standardizedTaskName) {
        PrivateTaskEntity privateTask = privateTaskRepository.findByProjectIdAndMemberIdAndLowercaseName(projectId, invokingMemberId, standardizedTaskName);

        if (privateTask == null) {
            privateTask = new PrivateTaskEntity();
            privateTask.setId(UUID.randomUUID());
            privateTask.setOrganizationId(organizationId);
            privateTask.setProjectId(projectId);
            privateTask.setMemberId(invokingMemberId);
            privateTask.setName(taskInputDto.getName());
            privateTask.setLowercaseName(standardizedTaskName);
            privateTask.setDescription(taskInputDto.getDescription());

            privateTaskRepository.save(privateTask);
        }


        return toDto(privateTask);
    }

    private TaskDto toDto(PrivateTaskEntity privateTask) {
        TaskDto privateTaskDto = privateTaskMapper.toApi(privateTask);
        privateTaskDto.setPrivate(true);
        return privateTaskDto;
    }

    private void validateProjectFound(String reference, ProjectEntity project) {
        if (project == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_PROJECT_REFERENCE, "Project {} not found", reference);
        }
    }

    public TaskDto getTask(UUID organizationId, UUID projectId, UUID invokingMemberId, UUID taskId) {

        ProjectEntity project = projectRepository.findByOrganizationIdAndId(organizationId, projectId);

        validateProjectFound(projectId.toString(), project);

        TaskEntity task = taskRepository.findByOrganizationIdAndProjectIdAndId(organizationId, projectId, taskId);

        if (task != null) {
            return toDto(project.isPrivate(), task);
        }

        PrivateTaskEntity privateTask = privateTaskRepository.findByOrganizationIdAndMemberIdAndId(organizationId, invokingMemberId, taskId);

        if (privateTask != null) {
            return toDto(privateTask);
        }
        return null;
    }

    public List<TaskDto> findTasksStartingWith(UUID orgId, UUID projectId, String startsWith) {
        validateProjectWithinOrg(orgId, projectId);

        validateSearchIncludesNumber(startsWith);

        List<TaskEntity> taskEntities = taskRepository.findTop10ByProjectIdAndLowercaseNameStartingWith(projectId, startsWith.toLowerCase());

        return taskMapper.toApiList(taskEntities);
    }

    private void validateSearchIncludesNumber(String startsWith) {
        if (startsWith == null || !startsWith.matches(".*\\d+.*")) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_SEARCH_STRING, "Task search string needs to minimally include a number");
        }
    }

    private void validateProjectWithinOrg(UUID orgId, UUID projectId) {
        ProjectEntity projectEntity = projectRepository.findByOrganizationIdAndId(orgId, projectId);

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
        return taskDto != null && TaskEntity.DEFAULT_TASK_NAME.equals(taskDto.getName());
    }

    public List<TaskDto> findTasksByRecentMemberAccess(UUID organizationId, UUID memberId, UUID projectId) {

        List<RecentAllTaskEntity> tasks = recentAllTaskRepository.findByRecentMemberAccess(organizationId, memberId, projectId);

        return recentAllTaskMapper.toApiList(tasks);
    }

    public TaskDto findDefaultTaskForProject(UUID organizationId, UUID projectId) {

        ProjectEntity project = projectRepository.findByOrganizationIdAndId(organizationId, projectId);
        validateProjectFound(projectId.toString(), project);

        TaskEntity defaultTask = taskRepository.findByOrganizationIdAndProjectIdAndLowercaseName(organizationId, projectId, TaskEntity.DEFAULT_TASK_NAME.toLowerCase());

        return toDto(project.isPrivate(), defaultTask);
    }

    public TaskDto findMostRecentTask(UUID organizationId, UUID memberId) {

        RecentAllTaskEntity activeTask = recentAllTaskRepository.findMostRecentTaskForMember(organizationId, memberId);

        TaskDto taskDto = null;
        if (activeTask != null) {
            ProjectEntity project = projectRepository.findByOrganizationIdAndId(organizationId, activeTask.getProjectId());
            validateProjectFound(activeTask.getProjectId().toString(), project);

            taskDto =  recentAllTaskMapper.toApi(activeTask);
        }

        return taskDto;
    }

    @Transactional
    public TaskDto createDefaultProjectTask(UUID organizationId, UUID projectId, boolean isPrivate) {

        TaskEntity defaultTask = new TaskEntity();
        defaultTask.setId(UUID.randomUUID());
        defaultTask.setOrganizationId(organizationId);
        defaultTask.setProjectId(projectId);
        defaultTask.configureDefault();

        taskRepository.save(defaultTask);

        return toDto(isPrivate, defaultTask);
    }


}