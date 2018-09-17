package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.RecentTasksByProjectDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class RecentActivityService {

    private static final int MAX_RECENT = 5;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RecentTaskRepository recentTaskRepository;

    @Autowired
    private RecentProjectRepository recentProjectRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ActiveWorkStatusRepository activeWorkStatusRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;
    private DtoEntityMapper<TaskDto, TaskEntity> taskMapper;

    @PostConstruct
    private void init() {
        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
        taskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, TaskEntity.class);
    }

    public void updateRecentProjects(IntentionEntity activeIntention) {

        List<RecentProjectEntity> recentProjects = recentProjectRepository.findByMemberId(activeIntention.getMemberId());

        RecentProjectEntity matchingProject = findMatchingProject(recentProjects, activeIntention.getProjectId());
        if (matchingProject != null) {
            matchingProject.setLastAccessed(LocalDateTime.now());
            recentProjectRepository.save(matchingProject);
        } else {
            deleteOldestProjectOverMaxRecent(recentProjects);

            RecentProjectEntity activeProject = createRecentProject(activeIntention);
            recentProjectRepository.save(activeProject);
        }

    }

    public void updateRecentTasks(IntentionEntity activeIntention) {

        List<RecentTaskEntity> recentTasks = recentTaskRepository.findByMemberIdAndProjectId(activeIntention.getMemberId(), activeIntention.getProjectId());

        RecentTaskEntity matchingTask = findMatchingTask(recentTasks, activeIntention.getTaskId());
        if (matchingTask != null) {
            matchingTask.setLastAccessed(LocalDateTime.now());
            recentTaskRepository.save(matchingTask);
        } else {
            deleteOldestTaskOverMaxRecent(recentTasks);

            RecentTaskEntity recentTask = createRecentTask(activeIntention);
            recentTaskRepository.save(recentTask);
        }

        ActiveWorkStatusEntity workStatus = activeWorkStatusRepository.findByMemberId(activeIntention.getMemberId());

        if (workStatus == null) {
            workStatus = new ActiveWorkStatusEntity();
            workStatus.setId(UUID.randomUUID());
            workStatus.setMemberId(activeIntention.getMemberId());
            workStatus.setOrganizationId(activeIntention.getOrganizationId());
        }

        workStatus.setActiveTaskId(activeIntention.getTaskId());
        workStatus.setLastUpdate(LocalDateTime.now());
        workStatus.setWorkingOn(activeIntention.getDescription());

        activeWorkStatusRepository.save(workStatus);
    }

    public RecentTasksByProjectDto getRecentTasksByProject(UUID organizationId, UUID memberId) {
        RecentTasksByProjectDto recentTasksByProjectDto = new RecentTasksByProjectDto();

        List<ProjectEntity> recentProjects = projectRepository.findByRecentMemberAccess(memberId);
        List<ProjectEntity> allProjects = projectRepository.findByOrganizationId(organizationId);

        List<ProjectEntity> recentProjectsWithDefaults = combineRecentProjectsWithDefaults(recentProjects, allProjects);

        List<ProjectDto> projectDtos = projectMapper.toApiList(recentProjectsWithDefaults);

        for (ProjectDto projectDto : projectDtos) {

            List<TaskEntity> recentTasks = taskRepository.findByRecentMemberAccess(memberId, projectDto.getId());
            List<TaskEntity> defaultTasks = taskRepository.findTop5ByProjectIdOrderByExternalId(projectDto.getId());
            List<TaskEntity> recentTasksWithDefaults = combineRecentTasksWithDefaults(recentTasks, defaultTasks);

            List<TaskDto> taskDtos = taskMapper.toApiList(recentTasksWithDefaults);

            recentTasksByProjectDto.addRecentProjectTasks(projectDto, taskDtos);
        }

        return recentTasksByProjectDto;
    }

    private List<TaskEntity> combineRecentTasksWithDefaults(List<TaskEntity> recentTasks, List<TaskEntity> defaultTasks) {

        Map<UUID, TaskEntity> recentTaskMap = new LinkedHashMap<>();

        for (TaskEntity recentTask : recentTasks) {
            recentTaskMap.put(recentTask.getId(), recentTask);
        }

        for (TaskEntity defaultTask : defaultTasks) {
            recentTaskMap.putIfAbsent(defaultTask.getId(), defaultTask);
        }

        return new ArrayList<>(recentTaskMap.values());
    }


    private List<ProjectEntity> combineRecentProjectsWithDefaults(List<ProjectEntity> recentProjects, List<ProjectEntity> defaultProjects) {
        Map<UUID, ProjectEntity> recentProjectMap = new LinkedHashMap<>();

        for (ProjectEntity recentProject : recentProjects) {
            recentProjectMap.put(recentProject.getId(), recentProject);
        }

        for (ProjectEntity defaultProject : defaultProjects) {
            recentProjectMap.putIfAbsent(defaultProject.getId(), defaultProject);
        }

        return new ArrayList<>(recentProjectMap.values());
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

    private RecentProjectEntity createRecentProject(IntentionEntity activeChunkEvent) {
        return RecentProjectEntity.builder()
                .id(UUID.randomUUID())
                .projectId(activeChunkEvent.getProjectId())
                .memberId(activeChunkEvent.getMemberId())
                .organizationId(activeChunkEvent.getOrganizationId())
                .lastAccessed(LocalDateTime.now()).build();
    }

    private RecentTaskEntity createRecentTask(IntentionEntity activeChunkEvent) {
        return RecentTaskEntity.builder()
                .id(UUID.randomUUID())
                .taskId(activeChunkEvent.getTaskId())
                .projectId(activeChunkEvent.getProjectId())
                .memberId(activeChunkEvent.getMemberId())
                .organizationId(activeChunkEvent.getOrganizationId())
                .lastAccessed(LocalDateTime.now()).build();
    }


}