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
import java.util.UUID;

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
    UserProfileDto register(@RequestBody RootAccountCredentialsInputDto rootAccountCredentialsInputDto) {
        log.info("register, user={}", rootAccountCredentialsInputDto.getEmail());

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
        log.info("activate, activationCode={}", activationCode.getActivationCode());

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
        log.info("reset, user={}", rootAccountEmailInputDto.getEmail());

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
        log.info("cycleKeys, user={}", context.getRootAccountId());

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
        log.info("heartbeat, user={}", context.getRootAccountId());

        return rootAccountCapability.heartbeat(context.getRootAccountId(), heartbeat);
    }

    /**
     * Login to the default organization using a username and password, (or email and password works too)
     *
     * Will return a temporary connectionId that can be used in lieu of an API-key
     * for the duration of the session
     *
     * Logging in this way, will not change your status to Online.
     *
     * Use the talk /connect API to go Online.
     *
     *
     * @return ConnectionStatusDto
     */

    @PreAuthorize("permitAll")
    @PostMapping(ResourcePaths.LOGIN_PATH + ResourcePaths.WITH_PATH + ResourcePaths.PASSWORD_PATH)
    ConnectionStatusDto loginWithPassword(@RequestBody RootLoginInputDto rootLoginInputDto) {
        log.info("loginWithPassword, user={}", rootLoginInputDto.getUsername());

        return rootAccountCapability.loginWithPassword(rootLoginInputDto.getUsername(), rootLoginInputDto.getPassword());
    }

    /**
     * Login to a specific organization using a username and password, (or email and password works too)
     *
     * Will return a temporary connectionId that can be used in lieu of an API-key
     * for the duration of the session
     *
     * Logging in this way, will not change your status to Online.
     *
     * Use the talk /connect API to go Online.
     *
     * @return ConnectionStatusDto
     */

    @PreAuthorize("permitAll")
    @PostMapping(ResourcePaths.LOGIN_PATH + ResourcePaths.TO_PATH + ResourcePaths.ORGANIZATION_PATH + "/{organizationId}" + ResourcePaths.WITH_PATH + ResourcePaths.PASSWORD_PATH)
    ConnectionStatusDto loginToOrganizationWithPassword(@PathVariable("organizationId") String organizationIdStr, @RequestBody RootLoginInputDto rootLoginInputDto) {
        log.info("loginToOrganizationWithPassword, user={}", rootLoginInputDto.getUsername());

        UUID organizationId = UUID.fromString(organizationIdStr);

        return rootAccountCapability.loginToOrganizationWithPassword(rootLoginInputDto.getUsername(), rootLoginInputDto.getPassword(), organizationId);
    }

    /**
     * Login to a specific organization.
     *
     * Login with the API-key and get a temporary connectionId that can be used in lieu of an API-key
     * for the duration of the session
     *
     * To login completely, talk has to handshake via the /connect API or the user isnt fully logged in
     * @return ConnectionStatusDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.LOGIN_PATH + ResourcePaths.TO_PATH + ResourcePaths.ORGANIZATION_PATH + "/{organizationId}")
    ConnectionStatusDto loginToOrganization(@PathVariable("organizationId") String organizationIdStr) {

        RequestContext context = RequestContext.get();
        log.info("loginToOrganization, user={}", context.getRootAccountId());

        UUID organizationId = UUID.fromString(organizationIdStr);
        return rootAccountCapability.loginToOrganization(context.getRootAccountId(), organizationId);
    }

    /**
     * Login to the default organization.
     *
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

        log.info("login, user={}", context.getRootAccountId());

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

        log.info("logout, user={}", context.getRootAccountId());

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

        log.info("connect, user={}", context.getRootAccountId());

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

        log.info("getProfile, user={}", context.getRootAccountId());
        return rootAccountCapability.getProfile(context.getRootAccountId());
    }

    /**
     * Updates the user's root account profile username (must be unique)
     * @return UserProfileDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.ROOT_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.USERNAME_PATH)
    UserProfileDto updateRootProfileUserName(@RequestBody UsernameInputDto usernameInputDto) {
        RequestContext context = RequestContext.get();
        log.info("updateRootProfileUsername, user={}", context.getRootAccountId());

        return rootAccountCapability.updateRootProfileUserName(context.getRootAccountId(), usernameInputDto.getUsername());
    }

    /**
     * Updates the user's active organization account profile username (must be unique within the org)
     * @return UserProfileDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.ORG_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.USERNAME_PATH)
    UserProfileDto updateOrgProfileUserName(@RequestBody UsernameInputDto usernameInputDto) {

        RequestContext context = RequestContext.get();

        log.info("updateOrgProfileUsername, user={}", context.getRootAccountId());

        return rootAccountCapability.updateOrgProfileUserName(context.getRootAccountId(), usernameInputDto.getUsername());
    }

    /**
     * Updates the user's root account profile email
     *
     * Sends a validation email to the new address, and the change is officially active on validation
     *
     * @return UserProfileDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.ROOT_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.EMAIL_PATH)
    UserProfileDto updateRootProfileEmail(@RequestBody EmailInputDto emailInputDto) {

        RequestContext context = RequestContext.get();

        log.info("updateRootProfileEmail, user={}", context.getRootAccountId());

        return rootAccountCapability.updateRootProfileEmail(context.getRootAccountId(), emailInputDto.getEmail());
    }

    /**
     * Updates the user's active organization account profile email
     *
     * Sends a validation email to the new address, and the change is officially active on validation
     *
     * @return UserProfileDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.ORG_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.EMAIL_PATH)
    UserProfileDto updateOrgProfileEmail(@RequestBody EmailInputDto emailInputDto) {

        RequestContext context = RequestContext.get();
        log.info("updateOrgProfileEmail, user={}", context.getRootAccountId());

        return rootAccountCapability.updateOrgProfileEmail(context.getRootAccountId(), emailInputDto.getEmail());
    }

    /**
     * Validates the change to the user's org account email, setting the new email officially into effect.
     *
     * @return SimpleStatusDto SUCCESS if the change succeeded, FAILED if the validation code is expired or cant be found
     */

    @PreAuthorize("permitAll")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.ORG_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.EMAIL_PATH + ResourcePaths.VALIDATE_PATH)
    SimpleStatusDto validateOrgProfileEmail(@RequestParam("validationCode") String validationCode) {

        log.info("validateOrgProfileEmail, validationCode={}", validationCode);

        return rootAccountCapability.validateOrgProfileEmail(validationCode);
    }

    /**
     * Validates the change to the user's root account email, setting the new email officially into effect.
     *
     * @return SimpleStatusDto SUCCESS if the change succeeded, FAILED if the validation code is expired or cant be found
     */

    @PreAuthorize("permitAll")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.ROOT_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.EMAIL_PATH + ResourcePaths.VALIDATE_PATH)
    SimpleStatusDto validateRootProfileEmail(@RequestParam("validationCode") String validationCode) {

        log.info("validateRootProfileEmail, validationCode={}", validationCode);

        return rootAccountCapability.validateRootProfileEmail(validationCode);
    }

    /**
     * Updates the user's root account full name
     * @return UserProfileDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.ROOT_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.FULLNAME_PATH)
    UserProfileDto updateRootProfileFullName(@RequestBody FullNameInputDto fullNameInputDto) {

        RequestContext context = RequestContext.get();

        log.info("updateRootProfileFullName, user={}", context.getRootAccountId());

        return rootAccountCapability.updateRootProfileFullName(context.getRootAccountId(), fullNameInputDto.getFullName());
    }

    /**
     * Updates the user's root account profile display name (usually first name)
     * @return UserProfileDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROFILE_PATH + ResourcePaths.ROOT_PATH + ResourcePaths.PROPERTY_PATH + ResourcePaths.DISPLAYNAME_PATH)
    UserProfileDto updateRootProfileDisplayName(@RequestBody DisplayNameInputDto displayNameInputDto) {

        RequestContext context = RequestContext.get();

        log.info("updateRootProfileDisplayName, user={}", context.getRootAccountId());

        return rootAccountCapability.updateRootProfileDisplayName(context.getRootAccountId(), displayNameInputDto.getDisplayName());
    }

}
