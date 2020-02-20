package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.*;
import com.dreamscale.gridtime.api.project.ProjectDto;
import com.dreamscale.gridtime.api.project.TaskDto;
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity;
import com.dreamscale.gridtime.core.domain.journal.TaskEntity;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.operator.RootAccountCapabilitty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.ACCOUNT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class AccountResource {

    @Autowired
    private RootAccountCapabilitty rootAccountCapabilitty;


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
     * Activate your root account to get a permanent API-Key to use for all future requests
     * that will be tied to your account
     * @param activationCode Provided by /organization/{id}/member service registration
     * @return AccountActivationDto
     */
    @PostMapping(ResourcePaths.ACTIVATE_PATH)
    AccountActivationDto activate(@RequestBody ActivationCodeDto activationCode) {

        return rootAccountCapabilitty.activate(activationCode.getActivationCode());
    }

    /**
     * Keep alive signal so we can tell when the user's go offline
     * @param heartbeat delta is the lag time between pings
     * @return SimpleStatusDto
     */
    @PostMapping(ResourcePaths.HEARTBEAT_PATH)
    SimpleStatusDto heartbeat(@RequestBody HeartbeatDto heartbeat) {

        RequestContext context = RequestContext.get();
        return rootAccountCapabilitty.heartbeat(context.getRootAccountId(), heartbeat);
    }

    /**
     * Login with the API-key and get a temporary connectionId that can be used in lieu of an API-key
     * for the duration of the session
     * @return ConnectionStatusDto
     */
    @PostMapping(ResourcePaths.LOGIN_PATH)
    ConnectionStatusDto login() {

        RequestContext context = RequestContext.get();
        return rootAccountCapabilitty.login(context.getRootAccountId());
    }

    /**
     * Logout the user so the temporary connectionId expires
     * @return SimpleStatusDto
     */
    @PostMapping(ResourcePaths.LOGOUT_PATH)
    SimpleStatusDto logout() {

        RequestContext context = RequestContext.get();
        return rootAccountCapabilitty.logout(context.getRootAccountId());
    }

}
