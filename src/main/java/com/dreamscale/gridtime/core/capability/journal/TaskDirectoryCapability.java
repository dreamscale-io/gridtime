package com.dreamscale.gridtime.core.capability.journal;

import com.dreamscale.gridtime.api.project.TaskDto;
import com.dreamscale.gridtime.api.project.TaskInputDto;
import com.dreamscale.gridtime.core.capability.external.JiraCapability;
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity;
import com.dreamscale.gridtime.core.domain.journal.ProjectRepository;
import com.dreamscale.gridtime.core.domain.journal.TaskEntity;
import com.dreamscale.gridtime.core.domain.journal.TaskRepository;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraTaskDto;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TaskDirectoryCapability {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private JiraCapability jiraCapability;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<TaskDto, TaskEntity> taskMapper;

    @PostConstruct
    private void init() {
        taskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, TaskEntity.class);
    }

    public TaskDto createNewTask(UUID organizationId, UUID projectId, UUID rootAccountId, TaskInputDto taskInputDto) {
        validateProjectWithinOrg(organizationId, projectId);

        ProjectEntity project = projectRepository.findById(projectId);
        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndRootAccountId(organizationId, rootAccountId);

        JiraTaskDto jiraTaskDto = jiraCapability.createNewTask(organizationId, project.getExternalId(), membership.getExternalId(), taskInputDto);

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(UUID.randomUUID());
        taskEntity.setName(jiraTaskDto.getKey());
        taskEntity.setSummary(jiraTaskDto.getSummary());
        taskEntity.setStatus(jiraTaskDto.getStatus());
        taskEntity.setExternalId(jiraTaskDto.getId());
        taskEntity.setProjectId(projectId);
        taskEntity.setOrganizationId(organizationId);

        taskRepository.save(taskEntity);

        return taskMapper.toApi(taskEntity);
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
}