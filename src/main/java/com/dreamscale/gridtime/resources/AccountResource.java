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
import com.dreamscale.gridtime.core.capability.active.RootAccountCapability;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.ACCOUNT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class AccountResource {

    @Autowired
    private RootAccountCapability rootAccountCapability;


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
     * Register your root account using an email address & password, will cause an activation email
     * to be sent.  The account will not be active, as part of the public organization, until activated.
     *
     * @param rootAccountCredentialsInputDto provide email & password for your account
     *
     * @return SimpleStatusDto returns status SUCCESS once email is sent
     */

    @PreAuthorize("permitAll")
    @PostMapping(ResourcePaths.REGISTER_PATH)
    SimpleStatusDto register(@RequestBody RootAccountCredentialsInputDto rootAccountCredentialsInputDto) {

        return rootAccountCapability.registerAccount(rootAccountCredentialsInputDto);
    }

    /**
     * Activate your root account to get a permanent API-Key to use for all future requests
     * that will be tied to your account.  Expected to be called when the activation code is typed into Torchie,
     * and the API-key returned to the app
     *
     * @param activationCode Provided by /account/register in the sent email
     *
     * @return AccountActivationDto
     */

    @PreAuthorize("permitAll")
    @PostMapping(ResourcePaths.ACTIVATE_PATH)
    AccountActivationDto activate(@RequestBody ActivationCodeDto activationCode) {

        return rootAccountCapability.activate(activationCode.getActivationCode());
    }

    /**
     * Resets the account, sending a new activation code to be sent to the user's email
     * The existing API-key will still work, until activate is called with the new code, and the API-key changes
     *
     * @return SimpleStatusDto returns status SENT if the email was sent
     */

    @PreAuthorize("permitAll")
    @PostMapping(ResourcePaths.RESET_PATH)
    SimpleStatusDto reset(@RequestBody RootAccountEmailInputDto rootAccountEmailInputDto) {

        return rootAccountCapability.reset(rootAccountEmailInputDto.getEmail());
    }


    /**
     * Cycle the keys used by the application, such that the old api-key will be used
     * to validate the current authentication, then a new API-key will be returned that can be used by
     * subsequent requests.
     *
     * @return AccountActivationDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.CYCLE_PATH + ResourcePaths.KEYS_PATH)
    AccountActivationDto cycleKeys() {

        RequestContext context = RequestContext.get();
        return rootAccountCapability.cycleKeys(context.getRootAccountId());
    }


    /**
     * Keep alive signal so we can tell when the user's go offline
     * @param heartbeat delta is the lag time between pings
     * @return SimpleStatusDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.HEARTBEAT_PATH)
    SimpleStatusDto heartbeat(@RequestBody HeartbeatDto heartbeat) {

        RequestContext context = RequestContext.get();
        return rootAccountCapability.heartbeat(context.getRootAccountId(), heartbeat);
    }

    /**
     * Login with the API-key and get a temporary connectionId that can be used in lieu of an API-key
     * for the duration of the session
     *
     * To login completely, talk has to handshake via the /connect API or the user isnt fully logged in
     * @return ConnectionStatusDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.LOGIN_PATH)
    ConnectionStatusDto login() {

        RequestContext context = RequestContext.get();
        return rootAccountCapability.login(context.getRootAccountId());
    }

    /**
     * Logout the user so the temporary connectionId expires
     * @return SimpleStatusDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.LOGOUT_PATH)
    SimpleStatusDto logout() {

        RequestContext context = RequestContext.get();
        return rootAccountCapability.logout(context.getRootAccountId());
    }

    /**
     * Handshake connectionId method used by talk after login, to get all the rooms needing to be reconnected.
     *
     * The user will be "Online" once this handshake is complete
     *
     * @return ActiveTalkConnectionDto
     */

    @PreAuthorize("permitAll")
    @PostMapping(ResourcePaths.CONNECT_PATH)
    ActiveTalkConnectionDto connect(@RequestBody ConnectionInputDto connectionInputDto) {

        RequestContext context = RequestContext.get();
        return rootAccountCapability.connect(connectionInputDto.getConnectionId());
    }


    /**
     * Retrieves the users profile information, their full name, username, email etc
     * @return AccountActivationDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.PROFILE_PATH)
    UserProfileDto getProfile() {

        RequestContext context = RequestContext.get();
        return rootAccountCapability.getProfile(context.getRootAccountId());
    }

    /**
     * Updates the user's root account profile username (must be unique)
     * @return UserProfileDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.USERNAME_PATH)
    UserProfileDto updateProfileUserName(@RequestBody UserNameInputDto userProfileInputDto) {

        RequestContext context = RequestContext.get();
        return rootAccountCapability.updateProfileUserName(context.getRootAccountId(), userProfileInputDto.getUsername());
    }

    /**
     * Updates the user's root account profile email
     * @return UserProfileDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.EMAIL_PATH)
    UserProfileDto updateProfileEmail(@RequestBody EmailInputDto emailInputDto) {

        RequestContext context = RequestContext.get();
        return rootAccountCapability.updateProfileEmail(context.getRootAccountId(), emailInputDto.getEmail());
    }

    /**
     * Validates the change to the user's root account email, setting the new email officially into effect.
     *
     * @return SimpleStatusDto SUCCESS if the change succeeded, FAILED if the validation code is expired or cant be found
     */

    @PreAuthorize("permitAll")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.EMAIL_PATH + ResourcePaths.VALIDATE_PATH)
    SimpleStatusDto validateProfileEmail(@RequestParam("validationCode") String validationCode) {

        return rootAccountCapability.validateProfileEmail(validationCode);
    }

    /**
     * Updates the user's root account full name
     * @return UserProfileDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.FULLNAME_PATH)
    UserProfileDto updateProfileFullName(@RequestBody FullNameInputDto fullNameInputDto) {

        RequestContext context = RequestContext.get();
        return rootAccountCapability.updateProfileFullName(context.getRootAccountId(), fullNameInputDto.getFullName());
    }

    /**
     * Updates the user's root account profile display name (usually first name)
     * @return UserProfileDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.DISPLAYNAME_PATH)
    UserProfileDto updateProfileDisplayName(@RequestBody DisplayNameInputDto displayNameInputDto) {

        RequestContext context = RequestContext.get();
        return rootAccountCapability.updateProfileDisplayName(context.getRootAccountId(), displayNameInputDto.getDisplayName());
    }


}
