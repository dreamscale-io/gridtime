package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.hooks.jira.*;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraProjectDto;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraTaskDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JiraSyncService {

    private static final String JIRA_SUMMARY = "summary";

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    TaskRepository taskRepository;

    public void synchronizeProjectsWithJira(UUID organizationId, JiraConnection jiraConnection) {
        List<ProjectEntity> dbProjects = projectRepository.findByOrganizationId(organizationId);
        List<JiraProjectDto> jiraProjects = jiraConnection.getProjects();

        saveProjectUpdates(organizationId, dbProjects, jiraProjects);
    }

    public void synchronizeOpenTasksWithJira(UUID organizationId, UUID projectId, JiraConnection jiraConnection) {

        ProjectEntity dbProject = projectRepository.findById(projectId);
        List<TaskEntity> dbTasks = taskRepository.findByProjectId(projectId);

        List<JiraTaskDto> jiraTasks = jiraConnection.getOpenTasksForProject(dbProject.getExternalId());

        saveTaskUpdates(organizationId, dbProject, dbTasks, jiraTasks);
    }

    private void saveProjectUpdates(UUID organizationId, List<ProjectEntity> dbProjects, List<JiraProjectDto> jiraProjects) {
        Map<String, ProjectEntity> dbProjectsByExternalId = createEntityMapByExternalId(dbProjects);

        for (JiraProjectDto jiraProject : jiraProjects) {
            ProjectEntity dbProject = dbProjectsByExternalId.get(jiraProject.getId());

            if (dbProject == null) {
                dbProject = createProjectEntityFromJiraProject(organizationId, jiraProject);
                projectRepository.save(dbProject);
            }
        }

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
    }

    private boolean isTaskUpdated(TaskEntity dbTask, JiraTaskDto jiraTask) {
        Object jiraSummary = jiraTask.getFields().get(JIRA_SUMMARY);
        String dbSummary = dbTask.getSummary();
        return jiraSummary != null && !jiraSummary.toString().equals(dbSummary);
    }

    private TaskEntity updateTaskFields(TaskEntity dbTask, JiraTaskDto jiraTask) {
        dbTask.setSummary(jiraTask.getFields().get(JIRA_SUMMARY).toString());
        return dbTask;
    }

    private TaskEntity createTaskEntityFromJiraTask(UUID organizationId, UUID projectId, JiraTaskDto jiraTask) {

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(UUID.randomUUID());
        taskEntity.setName(jiraTask.getKey());
        taskEntity.setSummary(jiraTask.getFields().get(JIRA_SUMMARY).toString());
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
