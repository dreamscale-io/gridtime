package com.dreamscale.gridtime.core.capability.external;

import com.dreamscale.gridtime.core.capability.journal.TaskCapability;
import com.dreamscale.gridtime.core.domain.journal.*;
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraProjectDto;
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraTaskDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JiraSyncCapability {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ConfigProjectSyncRepository configProjectSyncRepository;

    @Autowired
    JiraCapability jiraCapability;

    @Autowired
    TaskCapability taskCapability;


    public void synchronizeProjectsWithJira(UUID organizationId) {

        List<ConfigProjectSyncEntity> projectsToSync = configProjectSyncRepository.findByOrganizationId(organizationId);
        List<String> externalIds = extractExternalIds(projectsToSync);

        List<JiraProjectDto> jiraProjects = jiraCapability.getFilteredProjects(organizationId, externalIds);
        List<ProjectEntity> dbProjects = projectRepository.findPublicProjectsByOrganizationId(organizationId);

        saveProjectAndTaskUpdates(organizationId, dbProjects, jiraProjects);

    }

    private void saveProjectAndTaskUpdates(UUID organizationId, List<ProjectEntity> dbProjects, List<JiraProjectDto> jiraProjects) {
        Map<String, ProjectEntity> dbProjectsByExternalId = createEntityMapByExternalId(dbProjects);

        for (JiraProjectDto jiraProject : jiraProjects) {
            ProjectEntity dbProject = dbProjectsByExternalId.get(jiraProject.getId());

            if (dbProject == null) {
                dbProject = createProjectEntityFromJiraProject(organizationId, jiraProject);
                projectRepository.save(dbProject);
            }

            synchronizeDefaultTasks(organizationId, dbProject);
            synchronizeOpenTasks(organizationId, dbProject);
        }
    }

    private void synchronizeDefaultTasks(UUID organizationId, ProjectEntity dbProject) {
        TaskEntity defaultTask = taskRepository.findByOrganizationIdAndProjectIdAndLowercaseName(organizationId, dbProject.getId(), TaskEntity.DEFAULT_TASK_NAME.toLowerCase());

        //TODO this is hacked, but all this jira stuff is deprecated and on hold for now, til we figure out what we want to do with this
        // no sense on making this better.  It works for now.
        if (defaultTask == null) {
            defaultTask = new TaskEntity();
            defaultTask.setId(UUID.randomUUID());
            defaultTask.setProjectId(dbProject.getId());
            defaultTask.setOrganizationId(organizationId);

            taskCapability.createDefaultProjectTask(organizationId, dbProject.getId(), false);

        }
    }

    private void synchronizeOpenTasks(UUID organizationId, ProjectEntity dbProject) {

        List<TaskEntity> dbTasks = taskRepository.findByProjectId(dbProject.getId());

        List<JiraTaskDto> jiraTasks = jiraCapability.getOpenTasksForProject(organizationId, dbProject.getExternalId());

        saveTaskUpdates(organizationId, dbProject, dbTasks, jiraTasks);

    }


    private List<String> extractExternalIds(List<ConfigProjectSyncEntity> projectsToSync) {
        List<String> externalIds = new ArrayList<>();
        for (ConfigProjectSyncEntity project : projectsToSync) {
            externalIds.add(project.getProjectExternalId());
        }
        return externalIds;
    }


    private void saveTaskUpdates(UUID organizationId, ProjectEntity dbProject, List<TaskEntity> dbTasks, List<JiraTaskDto> jiraTasks) {
        Map<String, TaskEntity> dbTasksByExternalId = createEntityMapByExternalId(dbTasks);

        for (JiraTaskDto jiraTask : jiraTasks) {
            TaskEntity dbTask = dbTasksByExternalId.get(jiraTask.getId());

            if (dbTask == null) {
                dbTask = createTaskEntityFromJiraTask(organizationId, dbProject.getId(), jiraTask);
                taskRepository.save(dbTask);
            } else if (isTaskUpdated(dbTask, jiraTask)) {
                dbTask = updateTaskFields(dbTask, jiraTask);
                taskRepository.save(dbTask);
            }
        }

        List<TaskEntity> dbTasksNotOpenInJira = findDbTasksNotOpenInJira(dbTasks, jiraTasks);

        for (TaskEntity dbTaskNotInJira : dbTasksNotOpenInJira) {
            if (shouldBeInJira(dbTaskNotInJira)) {
                dbTaskNotInJira.setStatus("Done");
                taskRepository.save(dbTaskNotInJira);
            }
        }
    }

    private boolean shouldBeInJira(TaskEntity taskEntity) {
        return !taskEntity.isDefaultTask();
    }

    private List<TaskEntity> findDbTasksNotOpenInJira(List<TaskEntity> dbTasks, List<JiraTaskDto> jiraTasks) {
        Map<String, JiraTaskDto> jiraTasksById = new HashMap<>();

        for (JiraTaskDto jiraTask : jiraTasks) {
            jiraTasksById.put(jiraTask.getId(), jiraTask);
        }

        List<TaskEntity> dbTasksNotInJira = new ArrayList<>();

        for (TaskEntity dbTask : dbTasks) {
            if (jiraTasksById.get(dbTask.getExternalId()) == null) {
                dbTasksNotInJira.add(dbTask);
            }
        }

        return dbTasksNotInJira;
    }

    private boolean isTaskUpdated(TaskEntity dbTask, JiraTaskDto jiraTask) {
        String jiraSummary = jiraTask.getSummary();
        String dbSummary = dbTask.getDescription();

        boolean summaryIsDifferent = jiraSummary != null && !jiraSummary.equals(dbSummary);

        String jiraStatus = jiraTask.getStatus();
        String dbStatus = dbTask.getStatus();

        boolean statusIsDifferent = jiraStatus != null && !jiraStatus.equals(dbStatus);

        return summaryIsDifferent || statusIsDifferent;
    }


    private TaskEntity updateTaskFields(TaskEntity dbTask, JiraTaskDto jiraTask) {
        dbTask.setDescription(jiraTask.getSummary());
        dbTask.setStatus(jiraTask.getStatus());
        return dbTask;
    }

    private TaskEntity createTaskEntityFromJiraTask(UUID organizationId, UUID projectId, JiraTaskDto jiraTask) {

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(UUID.randomUUID());
        taskEntity.setName(jiraTask.getKey());
        taskEntity.setDescription(jiraTask.getSummary());
        taskEntity.setStatus(jiraTask.getStatus());
        taskEntity.setExternalId(jiraTask.getId());
        taskEntity.setProjectId(projectId);
        taskEntity.setOrganizationId(organizationId);

        return taskEntity;
    }

    private ProjectEntity createProjectEntityFromJiraProject(UUID organizationId, JiraProjectDto jiraProject) {
        ProjectEntity project = new ProjectEntity();
        project.setId(UUID.randomUUID());
        project.setName(jiraProject.getName());
        project.setExternalId(jiraProject.getId());
        project.setOrganizationId(organizationId);
        return project;
    }

    private <T extends External> Map<String, T> createEntityMapByExternalId(List<T> dbEntities) {
        Map<String, T> entityByExternalId = new HashMap<>();

        for (T entity : dbEntities) {
            entityByExternalId.put(entity.getExternalId(), entity);
        }

        return entityByExternalId;
    }

}
