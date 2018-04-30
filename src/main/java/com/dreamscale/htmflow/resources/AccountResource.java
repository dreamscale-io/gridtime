package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.account.*;
import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import com.dreamscale.htmflow.core.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping(path = ResourcePaths.ACCOUNT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class AccountResource {

    @Autowired
    private AccountService accountService;


    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;
    private DtoEntityMapper<TaskDto, TaskEntity> taskMapper;

    @PostConstruct
    private void init() {
        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
        taskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, TaskEntity.class);
    }

    /**
     * Activate your master account to get a permanent API-Key to use for all future requests
     * that will be tied to your account
     * @param activationCode Provided by /organization/{id}/member service registration
     * @return
     */
    @PostMapping(ResourcePaths.ACTIVATE_PATH)
    AccountActivationDto activate(@RequestBody ActivationCodeDto activationCode) {

        return accountService.activate(activationCode.getActivationCode());
    }

    @PostMapping(ResourcePaths.HEARTBEAT_PATH)
    SimpleStatusDto heartbeat(@RequestBody HeartbeatDto heartbeat) {
        return new SimpleStatusDto(Status.VALID, "beat.");
    }

    @PostMapping(ResourcePaths.LOGIN_PATH)
    SimpleStatusDto login() {
        return new SimpleStatusDto(Status.VALID, "Logged In");
    }

    @PostMapping(ResourcePaths.LOGOUT_PATH)
    SimpleStatusDto logout() {
        return new SimpleStatusDto(Status.VALID, "Logged Out");
    }

}
