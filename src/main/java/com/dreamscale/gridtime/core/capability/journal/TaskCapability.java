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
    private MapperFactory mapperFactory;

    private DtoEntityMapper<TaskDto, TaskEntity> taskMapper;

    private static final String DEFAULT_TASK_NAME = "No Task";

    @PostConstruct
    private void init() {
        taskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, TaskEntity.class);
    }

    @Transactional
    public TaskDto findOrCreateTask(UUID organizationId, UUID projectId, CreateTaskInputDto taskInputDto) {

        String standardizedTaskName = standardizeToLowerCase(taskInputDto.getName());

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

       return taskMapper.toApi(task);
    }


    public TaskDto getTask(UUID taskId) {

        TaskEntity task = taskRepository.findOne(taskId);

        if (task != null) {
            return taskMapper.toApi(task);
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


    public TaskDto findDefaultTaskForProject(UUID organizationId, UUID projectId) {

        TaskEntity defaultTask = taskRepository.findByOrganizationIdAndProjectIdAndLowercaseName(organizationId, projectId, DEFAULT_TASK_NAME.toLowerCase());

        return taskMapper.toApi(defaultTask);

    }

    public TaskDto findMostRecentTask(UUID organizationId, UUID memberId) {

        TaskEntity activeTask = taskRepository.findMostRecentTaskForMember(memberId);
        return taskMapper.toApi(activeTask);
    }

    @Transactional
    public TaskDto createDefaultProjectTask(UUID organizationId, UUID projectId) {

        TaskEntity defaultTask = new TaskEntity();
        defaultTask.setId(UUID.randomUUID());
        defaultTask.setOrganizationId(organizationId);
        defaultTask.setProjectId(projectId);
        defaultTask.configureDefaultTask();

        taskRepository.save(defaultTask);


        return taskMapper.toApi(defaultTask);
    }



}