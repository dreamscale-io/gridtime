package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.account.AccountKeyDto;
import com.dreamscale.htmflow.api.account.AccountStatusDto;
import com.dreamscale.htmflow.api.account.HeartbeatDto;
import com.dreamscale.htmflow.api.account.SimpleStatusDto;
import com.dreamscale.htmflow.api.journal.ChunkEventInputDto;
import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.core.domain.ProjectEntity;
import com.dreamscale.htmflow.core.domain.ProjectRepository;
import com.dreamscale.htmflow.core.domain.TaskEntity;
import com.dreamscale.htmflow.core.domain.TaskRepository;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping(path = ResourcePaths.ACCOUNT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class AccountResource {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;
    private DtoEntityMapper<TaskDto, TaskEntity> taskMapper;

    @PostConstruct
    private void init() {
        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
        taskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, TaskEntity.class);
    }

    @PostMapping(ResourcePaths.ACTIVATE_PATH)
    AccountStatusDto activate(@RequestBody AccountKeyDto accountKey) {
        return new AccountStatusDto();
    }

    @PostMapping(ResourcePaths.HEARTBEAT_PATH)
    SimpleStatusDto heartbeat(@RequestBody HeartbeatDto heartbeat) {
        return new SimpleStatusDto();
    }

    @PostMapping(ResourcePaths.LOGIN_PATH)
    SimpleStatusDto login() {
        return new SimpleStatusDto();
    }

    @PostMapping(ResourcePaths.LOGOUT_PATH)
    SimpleStatusDto logout() {
        return new SimpleStatusDto();
    }

}
