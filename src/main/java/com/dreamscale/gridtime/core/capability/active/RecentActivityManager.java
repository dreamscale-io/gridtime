package com.dreamscale.gridtime.core.capability.active;

import com.dreamscale.gridtime.api.project.ProjectDto;
import com.dreamscale.gridtime.api.project.RecentTasksSummaryDto;
import com.dreamscale.gridtime.api.project.TaskDto;
import com.dreamscale.gridtime.core.capability.integration.JiraCapability;
import com.dreamscale.gridtime.core.domain.active.*;
import com.dreamscale.gridtime.core.domain.journal.*;
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository;
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraTaskDto;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.service.GridClock;
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
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ActiveWorkStatusManager activeWorkStatusManager;

    @Autowired
    private JiraCapability jiraCapability;

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

    public void updateRecentTasks(IntentionEntity activeIntention, LocalDateTime now, Long nanoTime) {

        List<RecentTaskEntity> recentTasks = recentTaskRepository.findByMemberIdAndProjectId(activeIntention.getMemberId(), activeIntention.getProjectId());

        RecentTaskEntity matchingTask = findMatchingTask(recentTasks, activeIntention.getTaskId());
        if (matchingTask != null) {
            matchingTask.setLastAccessed(LocalDateTime.now());
            recentTaskRepository.save(matchingTask);

        } else if (isNotDefaultTask(activeIntention.getTaskId())) {
            deleteOldestTaskOverMaxRecent(recentTasks);

            RecentTaskEntity recentTask = createRecentTask(activeIntention);
            recentTaskRepository.save(recentTask);
        }

        activeWorkStatusManager.pushMemberWorkStatus(activeIntention, now, nanoTime);

    }


    public UUID lookupProjectIdOfMostRecentActivity(OrganizationMemberEntity memberEntity) {
        UUID projectId = null;

        RecentProjectEntity recentProjectEntity = recentProjectRepository.findFirst1ByMemberIdOrderByLastAccessedDesc(memberEntity.getId());
        if (recentProjectEntity != null) {
            projectId = recentProjectEntity.getProjectId();
        }
        return projectId;
    }


    private boolean isNotDefaultTask(UUID taskId) {
        TaskEntity taskEntity = taskRepository.findOne(taskId);

        return !(taskEntity != null && taskEntity.isDefaultTask());
    }

    public RecentTasksSummaryDto getRecentTasksByProject(UUID organizationId, UUID memberId) {

        RecentTasksSummaryDto recentTasksSummaryDto = new RecentTasksSummaryDto();

        List<ProjectEntity> recentProjects = projectRepository.findByRecentMemberAccess(memberId);
        List<ProjectEntity> allProjects = projectRepository.findByOrganizationId(organizationId);

        List<ProjectEntity> recentProjectsWithDefaults = combineRecentProjectsWithDefaults(recentProjects, allProjects);

        List<ProjectDto> projectDtos = projectMapper.toApiList(recentProjectsWithDefaults);

        for (ProjectDto projectDto : projectDtos) {

            List<TaskEntity> recentTasks = taskRepository.findByRecentMemberAccess(memberId, projectDto.getId());
            List<TaskEntity> defaultTasks = taskRepository.findTop5ByProjectIdOrderByExternalIdDesc(projectDto.getId());

            TaskEntity noTaskDefaultTask = taskRepository.findByProjectIdAndName(projectDto.getId(), TaskEntity.DEFAULT_TASK_NAME);

            List<TaskEntity> recentTasksWithDefaults = combineRecentTasksWithDefaults(recentTasks, defaultTasks, noTaskDefaultTask);

            List<TaskDto> taskDtos = taskMapper.toApiList(recentTasksWithDefaults);

            recentTasksSummaryDto.addRecentProjectTasks(projectDto, taskDtos);
        }

        return recentTasksSummaryDto;
    }

    public RecentTasksSummaryDto createTaskReferenceInJournal(UUID organizationId, UUID memberId, String taskName) {

        List<TaskEntity> taskEntities = taskRepository.findByOrganizationIdAndName(organizationId, taskName);
        TaskEntity taskEntity = null;
        if (taskEntities.size() > 0) {
            taskEntity = taskEntities.get(0);
        }

        if (taskEntity == null) {
            taskEntity = findTaskInJiraAndUpdateDB(organizationId, taskName);
        }

        TaskDto taskDto = taskMapper.toApi(taskEntity);

        if (taskDto != null) {

            List<RecentTaskEntity> recentTasks = recentTaskRepository.findByMemberIdAndProjectId(memberId, taskDto.getProjectId());
            RecentTaskEntity matchingTask = findMatchingTask(recentTasks, taskDto.getId());

            if (matchingTask != null) {
                matchingTask.setLastAccessed(LocalDateTime.now());
                recentTaskRepository.save(matchingTask);
            } else {
                deleteOldestTaskOverMaxRecent(recentTasks);

                RecentTaskEntity recentTask = createRecentTask(organizationId, memberId, taskDto);
                recentTaskRepository.save(recentTask);
            }
        }

        RecentTasksSummaryDto recentTasksSummaryDto = getRecentTasksByProject(organizationId, memberId);
        recentTasksSummaryDto.setActiveTask(taskDto);

        return recentTasksSummaryDto;
    }

    private TaskEntity findTaskInJiraAndUpdateDB(UUID organizationId, String taskName) {

        TaskEntity taskEntity = null;

        //try Jira as a fallback if we can't find the task
        OrganizationEntity org = organizationRepository.findById(organizationId);
        if (org != null) {

            JiraTaskDto jiraTask = jiraCapability.getTask(organizationId, taskName);
            if (jiraTask != null) {

                ProjectEntity projectEntity = projectRepository.findByExternalId(jiraTask.getProjectId());

                if (projectEntity != null) {
                    taskEntity = new TaskEntity();
                    taskEntity.setId(UUID.randomUUID());
                    taskEntity.setName(jiraTask.getKey());
                    taskEntity.setSummary(jiraTask.getSummary());
                    taskEntity.setStatus(jiraTask.getStatus());
                    taskEntity.setExternalId(jiraTask.getId());
                    taskEntity.setProjectId(projectEntity.getId());
                    taskEntity.setOrganizationId(organizationId);
                    taskRepository.save(taskEntity);
                }
            }
        }
        return taskEntity;
    }

    private List<TaskEntity> combineRecentTasksWithDefaults
            (List<TaskEntity> recentTasks, List<TaskEntity> defaultTasks, TaskEntity noTaskTask) {

        Map<UUID, TaskEntity> recentTaskMap = new LinkedHashMap<>();

        int numberAdded = 0;

        //either add the noTaskTask as the 5th last entry, if it's not among recent
        for (TaskEntity recentTask : recentTasks) {
            recentTaskMap.put(recentTask.getId(), recentTask);
            numberAdded++;

            if (numberAdded == 4 && recentTaskMap.get(noTaskTask.getId()) == null) {
                recentTaskMap.putIfAbsent(noTaskTask.getId(), noTaskTask);
                break;
            }
        }

        //or if there's not yet 4 entries in recent, add to the top
        if (noTaskTask != null) {
            recentTaskMap.putIfAbsent(noTaskTask.getId(), noTaskTask);
        }

        for (TaskEntity defaultTask : defaultTasks) {
            if (recentTaskMap.size() < 5) {
                recentTaskMap.putIfAbsent(defaultTask.getId(), defaultTask);
            }
        }

        return new ArrayList<>(recentTaskMap.values());
    }


    private List<ProjectEntity> combineRecentProjectsWithDefaults
            (List<ProjectEntity> recentProjects, List<ProjectEntity> defaultProjects) {
        Map<UUID, ProjectEntity> recentProjectMap = new LinkedHashMap<>();

        for (ProjectEntity recentProject : recentProjects) {
            recentProjectMap.put(recentProject.getId(), recentProject);
        }

        for (ProjectEntity defaultProject : defaultProjects) {
            if (recentProjectMap.size() < 5) {
                recentProjectMap.putIfAbsent(defaultProject.getId(), defaultProject);
            }
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

    private RecentTaskEntity createRecentTask(UUID organizationId, UUID memberId, TaskDto taskDto) {
        return RecentTaskEntity.builder()
                .id(UUID.randomUUID())
                .taskId(taskDto.getId())
                .projectId(taskDto.getProjectId())
                .memberId(memberId)
                .organizationId(organizationId)
                .lastAccessed(LocalDateTime.now()).build();
    }



}