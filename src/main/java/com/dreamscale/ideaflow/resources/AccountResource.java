package com.dreamscale.ideaflow.resources;

import com.dreamscale.ideaflow.api.ResourcePaths;
import com.dreamscale.ideaflow.api.account.*;
import com.dreamscale.ideaflow.api.project.ProjectDto;
import com.dreamscale.ideaflow.api.project.TaskDto;
import com.dreamscale.ideaflow.core.domain.*;
import com.dreamscale.ideaflow.core.mapper.DtoEntityMapper;
import com.dreamscale.ideaflow.core.mapper.MapperFactory;
import com.dreamscale.ideaflow.core.security.RequestContext;
import com.dreamscale.ideaflow.core.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@Slf4j
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
     * @return AccountActivationDto
     */
    @PostMapping(ResourcePaths.ACTIVATE_PATH)
    AccountActivationDto activate(@RequestBody ActivationCodeDto activationCode) {

        return accountService.activate(activationCode.getActivationCode());
    }

    /**
     * Keep alive signal so we can tell when the user's go offline
     * @param heartbeat delta is the lag time between pings
     * @return SimpleStatusDto
     */
    @PostMapping(ResourcePaths.HEARTBEAT_PATH)
    SimpleStatusDto heartbeat(@RequestBody HeartbeatDto heartbeat) {

        RequestContext context = RequestContext.get();
        return accountService.heartbeat(context.getMasterAccountId(), heartbeat);
    }

    /**
     * Login with the API-key and get a temporary connectionId that can be used in lieu of an API-key
     * for the duration of the session
     * @return ConnectionStatusDto
     */
    @PostMapping(ResourcePaths.LOGIN_PATH)
    ConnectionStatusDto login() {

        RequestContext context = RequestContext.get();
        return accountService.login(context.getMasterAccountId());
    }

    /**
     * Logout the user so the temporary connectionId expires
     * @return SimpleStatusDto
     */
    @PostMapping(ResourcePaths.LOGOUT_PATH)
    SimpleStatusDto logout() {

        RequestContext context = RequestContext.get();
        return accountService.logout(context.getMasterAccountId());
    }

}
