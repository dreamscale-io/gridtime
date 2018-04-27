package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.account.*;
import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.ProjectEntity;
import com.dreamscale.htmflow.core.domain.ProjectRepository;
import com.dreamscale.htmflow.core.domain.TaskEntity;
import com.dreamscale.htmflow.core.domain.TaskRepository;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Collections;
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

//    POST /account/activate
//
//    request:
//    apiKey:{string}
//
//    respone:
//    status; {VALID, FAILED}
//    message: {string}
//    email: {string}

    @PostMapping(ResourcePaths.ACTIVATE_PATH)
    AccountActivationDto activate(@RequestBody ActivationTokenDto activationToken) {
        AccountActivationDto activation = new AccountActivationDto();
        activation.setStatus(Status.VALID);
        activation.setEmail("kara@dreamscale.io");
        activation.setMessage("Your account has been successfully activated.");
        activation.setApiKey("FASFD423fsfd32d2322d");

        return activation;
    }


//    GET /account/heartbeat
//    request:
//    header.apiKey: {string}
//    idleTime: {int}
//    deltaTime: {int}

    @PostMapping(ResourcePaths.HEARTBEAT_PATH)
    SimpleStatusDto heartbeat(@RequestBody HeartbeatDto heartbeat) {
        return new SimpleStatusDto();
    }


//    POST /account/login
//
//    request:
//    header.apiKey: {string}
//
//    reponse:
//    status; {VALID, FAILED}
//    message: {string}
//
//    POST /account/logout
//
//    request:
//    header.apiKey : {string}
//
//    response:
//    status; {VALID, FAILED}
//    message: {string}

    @PostMapping(ResourcePaths.LOGIN_PATH)
    SimpleStatusDto login() {
        return new SimpleStatusDto();
    }

    @PostMapping(ResourcePaths.LOGOUT_PATH)
    SimpleStatusDto logout() {
        return new SimpleStatusDto();
    }

}
