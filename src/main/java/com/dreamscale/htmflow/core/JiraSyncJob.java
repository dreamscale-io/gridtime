package com.dreamscale.htmflow.core;

import com.dreamscale.htmflow.core.domain.ProjectEntity;
import com.dreamscale.htmflow.core.domain.ProjectRepository;
import com.dreamscale.htmflow.core.domain.TaskEntity;
import com.dreamscale.htmflow.core.domain.TaskRepository;
import com.dreamscale.htmflow.core.hooks.jira.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JiraSyncJob {

    @Autowired
    JiraConnectionFactory jiraConnectionFactory;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    TaskRepository taskRepository;

    public void synchonizeDBWithJira(JiraConnection jiraConnection) {
        List<ProjectEntity> projects = fetchJiraProjects(jiraConnection);
        saveProjects(projects);

        for (ProjectEntity project : projects) {
            List<TaskEntity> tasks = fetchJiraTasks(jiraConnection, project);
            saveTasks(tasks);
        }
    }

    private void saveProjects(List<ProjectEntity> projects) {
        for (ProjectEntity project : projects) {
            ProjectEntity existingProject = projectRepository.findByExternalId(project.getExternalId());
            if (existingProject != null) {
                project.setId(existingProject.getId());
            }
            projectRepository.save(project);
        }
    }

    private void saveTasks(List<TaskEntity> tasks) {
        for (TaskEntity task : tasks) {
            TaskEntity existingTask = taskRepository.findByExternalId(task.getExternalId());
            if (existingTask != null) {
                task.setId(existingTask.getId());
            }
            taskRepository.save(task);
        }
    }

    private List<TaskEntity> fetchJiraTasks(JiraConnection jiraConnection, ProjectEntity project) {
        JiraSearchResultPage jiraSearchResults = jiraConnection.getOpenTasksForProject(project.getExternalId());

        List<TaskEntity> taskEntities = new ArrayList<>();

        for (JiraTaskDto jiraTask: jiraSearchResults.getIssues()) {
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setId(UUID.randomUUID());
            taskEntity.setName(jiraTask.getKey());
            taskEntity.setSummary(jiraTask.getFields().get("summary"));
            taskEntity.setExternalId(jiraTask.getId());
            taskEntity.setProjectId(project.getId());
            taskEntities.add(taskEntity);
        }

        return taskEntities;
    }

    private List<ProjectEntity> fetchJiraProjects(JiraConnection jiraConnection ) {
        List<JiraProjectDto> jiraProjects = jiraConnection.getProjects();

        List<ProjectEntity> projectEntities = new ArrayList<>();

        for (JiraProjectDto jiraProject: jiraProjects) {
            ProjectEntity project = new ProjectEntity();
            project.setId(UUID.randomUUID());
            project.setName(jiraProject.getName());
            project.setExternalId(jiraProject.getId());
            projectEntities.add(project);
        }

        return projectEntities;
    }
}
