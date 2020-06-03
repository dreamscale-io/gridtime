package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.*
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.api.invitation.InvitationKeyInputDto
import com.dreamscale.gridtime.api.organization.OrganizationSubscriptionDto
import com.dreamscale.gridtime.api.organization.SubscriptionInputDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.api.terminal.Command
import com.dreamscale.gridtime.api.terminal.CommandManualDto
import com.dreamscale.gridtime.api.terminal.CommandManualPageDto
import com.dreamscale.gridtime.api.terminal.RunCommandInputDto
import com.dreamscale.gridtime.client.*
import com.dreamscale.gridtime.core.capability.integration.EmailCapability
import com.dreamscale.gridtime.core.domain.member.*
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
class TerminalResourceSpec extends Specification {

    @Autowired
    TerminalClient terminalClient

    @Autowired
    InviteToClient inviteToClient

    @Autowired
    InvitationClient invitationClient

    @Autowired
    OrganizationClient organizationClient

    @Autowired
    SubscriptionClient subscriptionClient

    @Autowired
    TeamClient teamClient

    @Autowired
    AccountClient accountClient

    @Autowired
    RootAccountEntity testUser

    @Autowired
    RootAccountRepository rootAccountRepository

    @Autowired
    EmailCapability mockEmailCapability

    @Autowired
    GridClock mockTimeService;

    String activationCode = null;

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()
        mockTimeService.nanoTime() >> System.nanoTime()
    }

    def "should test terminal loop for a basic invite command"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        when:

        accountClient.login()

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")

        accountClient.logout()
        accountClient.login()

        1 * mockEmailCapability.sendDownloadActivateAndOrgInviteEmail(_, _, _) >> { emailAddr, org, token -> activationCode = token;
            return new SimpleStatusDto(Status.SENT, "Sent!")}

        TalkMessageDto inviteResult = terminalClient.runCommand(new RunCommandInputDto(Command.INVITE, "zoe@dreamscale.io", "to", "org"))

        then:
        assert activationCode != null
        assert inviteResult != null
    }

    def "should invite with user names"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto zoeProfile = register("zoe@dreamscale.io");

        switchUser(zoeProfile)

        accountClient.login()
        accountClient.updateOrgProfileUsername(new UsernameInputDto("zoe"))

        switchUser(artyProfile)

        accountClient.login()
        TeamDto team = teamClient.createTeam("Phoenix")

        when:

        TalkMessageDto inviteResult = terminalClient.runCommand(new RunCommandInputDto(Command.INVITE, "zoe", "to", "team"))

        switchUser(zoeProfile)

        TeamDto zoesTeam = teamClient.getMyHomeTeam();

        then:
        assert inviteResult != null

        assert inviteResult.messageType == "SimpleStatusDto"
        assert ((SimpleStatusDto)inviteResult.data).status == Status.JOINED;

        assert  zoesTeam.getName() == team.getName()

    }

    def "should invite to public from terminal"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        when:

        accountClient.login()

        1 * mockEmailCapability.sendDownloadAndInviteToPublicEmail(_, _, _, _) >> { from, emailAddr, org, token -> activationCode = token;
            return new SimpleStatusDto(Status.SENT, "Sent!")}

        TalkMessageDto inviteResult = terminalClient.runCommand(new RunCommandInputDto(Command.INVITE, "zoe@dreamscale.io", "to", "public"))

        AccountActivationDto accountActivation = accountClient.activate(new ActivationCodeDto(activationCode: activationCode))

        then:
        assert activationCode != null
        assert inviteResult != null
        assert accountActivation.status == Status.VALID
        assert accountActivation.apiKey != null

    }

    def "should get terminal help manual"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        when:

        accountClient.login()

        CommandManualDto manual = terminalClient.getCommandManual()
        CommandManualPageDto manualPage = terminalClient.getCommandManualPage("invite");

        println manualPage

        then:
        assert manual != null
        assert manual.getManualPages().size() == 1

        assert manualPage != null
        assert manualPage.command == Command.INVITE
        assert manualPage.description != null

        assert manualPage.terminalRoutes.size() == 1
        assert manualPage.terminalRoutes.get(0).argsTemplate != null
        assert manualPage.terminalRoutes.get(0).optionsHelp.size() == 2

    }

    private void switchUser(AccountActivationDto artyProfile) {
        RootAccountEntity account = rootAccountRepository.findByApiKey(artyProfile.getApiKey());

        testUser.setId(account.getId())
        testUser.setApiKey(account.getApiKey())
    }

    private AccountActivationDto register(String email) {

        RootAccountCredentialsInputDto rootAccountInput = new RootAccountCredentialsInputDto();
        rootAccountInput.setEmail(email)

        String inviteKey = null;

        1 * mockEmailCapability.sendDownloadAndActivationEmail(_, _) >> { emailAddr, token -> inviteKey = token; return null}

        UserProfileDto userProfileDto = accountClient.register(rootAccountInput)
        return accountClient.activate(new ActivationCodeDto(inviteKey))
    }

    private String inviteToOrgWithEmail(String email) {

        String activationToken = null;

        1 * mockEmailCapability.sendDownloadActivateAndOrgInviteEmail(_, _, _) >> { emailAddr, org, token -> activationToken = token; return null}

        inviteToClient.inviteToActiveOrganization(new EmailInputDto(email))

        return activationToken;
    }

    private OrganizationSubscriptionDto createSubscriptionAndValidateEmail(String domain, String ownerEmail) {
        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput(domain, ownerEmail)

        String validateToken = null;

        1 * mockEmailCapability.sendEmailToValidateOrgEmailAddress(_, _) >> { emailAddr, token -> validateToken = token; return null}

        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        invitationClient.useInvitationKey(new InvitationKeyInputDto(validateToken))

        return dreamScaleSubscription;
    }

    private OrganizationSubscriptionDto createSubscription(String domain, String ownerEmail) {
        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput(domain, ownerEmail)

        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        return dreamScaleSubscription;
    }

    private SubscriptionInputDto createSubscriptionInput(String domain, String ownerEmail) {
        SubscriptionInputDto orgSubscription = new SubscriptionInputDto()
        orgSubscription.setOrganizationName("MemberCompany")
        orgSubscription.setDomainName(domain)
        orgSubscription.setRequireMemberEmailInDomain(true)
        orgSubscription.setSeats(10)
        orgSubscription.setOwnerEmail(ownerEmail)
        orgSubscription.setStripePaymentId("[payment.id]")

        return orgSubscription
    }


}
