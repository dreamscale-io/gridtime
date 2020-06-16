package com.dreamscale.gridtime.core.capability.active;

import com.dreamscale.gridtime.api.project.ProjectDto;
import com.dreamscale.gridtime.api.project.TaskDto;
import com.dreamscale.gridtime.core.capability.external.JiraCapability;
import com.dreamscale.gridtime.core.capability.membership.TeamCapability;
import com.dreamscale.gridtime.core.domain.active.*;
import com.dreamscale.gridtime.core.domain.journal.*;
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class RecentActivityManager {

    private static final int MAX_RECENT = 5;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RecentTaskRepository recentTaskRepository;

    @Autowired
    private RecentProjectRepository recentProjectRepository;

    @Autowired
    private ActiveWorkStatusManager activeWorkStatusManager;

    @Autowired
    private JiraCapability jiraCapability;

    @Autowired
    private TeamCapability teamCapability;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;
    private DtoEntityMapper<TaskDto, TaskEntity> taskMapper;

    @PostConstruct
    private void init() {
        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
        taskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, TaskEntity.class);
    }


    public UUID lookupProjectIdOfMostRecentActivity(UUID organizationId, UUID memberId) {
        UUID projectId = null;

        RecentProjectEntity recentProjectEntity = recentProjectRepository.findFirst1ByMemberIdOrderByLastAccessedDesc(memberId);
        if (recentProjectEntity != null) {
            projectId = recentProjectEntity.getProjectId();
        }
        return projectId;
    }


    public void updateRecentTasks(LocalDateTime now, UUID organizationId, UUID memberId, TaskDto taskDto) {

        List<RecentTaskEntity> recentTasks = recentTaskRepository.findByMemberIdAndProjectId(memberId, taskDto.getProjectId());
        RecentTaskEntity matchingTask = findMatchingTask(recentTasks, taskDto.getId());

        if (matchingTask != null) {
            matchingTask.setLastAccessed(LocalDateTime.now());
            recentTaskRepository.save(matchingTask);
        } else {
            deleteOldestTaskOverMaxRecent(recentTasks);

            RecentTaskEntity recentTask = createRecentTask(now, organizationId, memberId, taskDto);
            recentTaskRepository.save(recentTask);
        }

    }


    public void updateRecentProjects(LocalDateTime now, UUID organizationId, UUID memberId, UUID projectId) {

        List<RecentProjectEntity> recentProjects = recentProjectRepository.findByMemberId(memberId);

        RecentProjectEntity matchingProject = findMatchingProject(recentProjects, projectId);
        if (matchingProject != null) {
            matchingProject.setLastAccessed(now);
            recentProjectRepository.save(matchingProject);
        } else {
            deleteOldestProjectOverMaxRecent(recentProjects);

            RecentProjectEntity activeProject = createRecentProject(now, organizationId, memberId, projectId);
            recentProjectRepository.save(activeProject);
        }

    }


    private RecentProjectEntity findMatchingProject(List<RecentProjectEntity> recentProjects, UUID projectId) {
        RecentProjectEntity matchingProject = null;

        for (RecentProjectEntity projectEntity : recentProjects) {
            if (projectEntity.getProjectId().equals(projectId)) {
                matchingProject = projectEntity;
                break;
            }
        }
        return matchingProject;
    }

    private RecentTaskEntity findMatchingTask(List<RecentTaskEntity> recentTasks, UUID taskId) {

        RecentTaskEntity matchingTask = null;

        for (RecentTaskEntity taskEntity : recentTasks) {
            if (taskEntity.getTaskId().equals(taskId)) {
                matchingTask = taskEntity;
                break;
            }
        }
        return matchingTask;
    }

    private void deleteOldestProjectOverMaxRecent(List<RecentProjectEntity> recentProjects) {

        if (recentProjects.size() >= MAX_RECENT) {

            RecentProjectEntity oldestProject = recentProjects.get(0);
            for (RecentProjectEntity project : recentProjects) {
                if (project.getLastAccessed().isBefore(oldestProject.getLastAccessed())) {
                    oldestProject = project;
                }
            }

            recentProjectRepository.delete(oldestProject);
        }
    }

    private void deleteOldestTaskOverMaxRecent(List<RecentTaskEntity> recentTasks) {

        if (recentTasks.size() >= MAX_RECENT) {

            RecentTaskEntity oldestTask = recentTasks.get(0);
            for (RecentTaskEntity task : recentTasks) {
                if (task.getLastAccessed().isBefore(oldestTask.getLastAccessed())) {
                    oldestTask = task;
                }
            }

            recentTaskRepository.delete(oldestTask);
        }
    }

    private RecentProjectEntity createRecentProject(LocalDateTime now, UUID organizationId, UUID memberId, UUID projectId) {
        return RecentProjectEntity.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .memberId(memberId)
                .organizationId(organizationId)
                .lastAccessed(now).build();
    }

    private RecentTaskEntity createRecentTask(LocalDateTime now, UUID organizationId, UUID memberId, TaskDto taskDto) {
        return RecentTaskEntity.builder()
                .id(UUID.randomUUID())
                .taskId(taskDto.getId())
                .projectId(taskDto.getProjectId())
                .memberId(memberId)
                .organizationId(organizationId)
                .lastAccessed(now).build();
    }



}