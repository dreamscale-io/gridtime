package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.*
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.api.grid.GridStatus
import com.dreamscale.gridtime.api.grid.GridStatusSummaryDto
import com.dreamscale.gridtime.api.grid.GridTableResults
import com.dreamscale.gridtime.api.invitation.InvitationKeyInputDto
import com.dreamscale.gridtime.api.organization.OrganizationSubscriptionDto
import com.dreamscale.gridtime.api.organization.SubscriptionInputDto
import com.dreamscale.gridtime.api.project.CreateProjectInputDto
import com.dreamscale.gridtime.api.project.ProjectDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.api.terminal.*
import com.dreamscale.gridtime.client.*
import com.dreamscale.gridtime.core.capability.external.EmailCapability
import com.dreamscale.gridtime.core.capability.external.JiraCapability
import com.dreamscale.gridtime.core.capability.system.GridClock
import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity
import com.dreamscale.gridtime.core.domain.flow.FlowActivityMetadataField
import com.dreamscale.gridtime.core.domain.flow.FlowActivityRepository
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity
import com.dreamscale.gridtime.core.domain.journal.TaskEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.machine.GridTimeEngine
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableFlowActivity
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom
import static com.dreamscale.gridtime.core.CoreARandom.aRandom
import static com.dreamscale.gridtime.core.CoreARandom.aRandom
import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class GridResourceSpec extends Specification {

    @Autowired
    GridClient gridClient

    @Autowired
    GridTimeEngine gridTimeEngine

    @Autowired
    FlowActivityRepository flowActivityRepository

    @Autowired
    AccountClient accountClient

    @Autowired
    RootAccountEntity testUser

    @Autowired
    GridClock gridClock

    UUID torchieId
    UUID teamId

    LocalDateTime clockStart
    LocalDateTime time1
    LocalDateTime time2

    OrganizationEntity org

    TeamEntity team
    ProjectEntity projectEntity

    @Autowired
    EmailCapability mockEmailCapability

    @Autowired
    RootAccountRepository rootAccountRepository

    def setup() {
        torchieId = UUID.randomUUID()
        teamId = UUID.randomUUID()

        projectEntity = aRandom.projectEntity().save();

        org = aRandom.organizationEntity().save()
        team = aRandom.teamEntity().id(teamId).organizationId(org.id).save()

        clockStart = gridClock.getGridStart()
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(45)

        gridTimeEngine.reset()
    }

    def "should start the gridtime server and show status of started"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        gridTimeEngine.configureDoneAfterTicks(12)

        when:

        SimpleStatusDto statusAfterStart = gridClient.start()

        GridStatusSummaryDto statusBefore = gridClient.getStatus()

        println statusBefore.activitySummary.toDisplayString()

        gridTimeEngine.waitForDone()

        GridStatusSummaryDto statusAfter = gridClient.getStatus()

        println statusAfter.activitySummary.toDisplayString()

        then:
        assert statusAfterStart != null
        assert statusAfterStart.getStatus() == Status.SUCCESS

        assert statusBefore != null
        assert statusBefore.getGridStatus() == GridStatus.RUNNING

        assert statusAfter != null
        assert statusAfter.getGridStatus() == GridStatus.STOPPED
    }

    def "should shutdown the gridtime server and show shutdown status"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        gridTimeEngine.configureDoneAfterTicks(12)

        when:

        SimpleStatusDto startStatus = gridClient.start()

        SimpleStatusDto shutdownStatus = gridClient.shutdown()

        GridStatusSummaryDto statusAfter = gridClient.getStatus()

        println statusAfter.activitySummary.toDisplayString()

        then:
        assert startStatus != null
        assert startStatus.getStatus() == Status.SUCCESS

        assert shutdownStatus != null
        assert shutdownStatus.getStatus() == Status.SUCCESS

        assert statusAfter != null
        assert statusAfter.getGridStatus() == GridStatus.STOPPED

    }

    def "should generate detailed process reports of different types"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        List<OrganizationMemberEntity> teamMembers = createTeamOfMembers(10);

        for (OrganizationMemberEntity member : teamMembers) {
            createIntention(member.getId(), time1)
            createActivity(member.getId(), time2, "/box/file1.java")
        }

        gridTimeEngine.configureDoneAfterTicks(12)
        SimpleStatusDto statusAfterStart = gridClient.start()
        gridTimeEngine.waitForDone()

        when:

        GridTableResults topReport = gridClient.getTopProcesses();
        GridTableResults topTorchie = gridClient.getTopTorchieProcesses();
        GridTableResults topPlexer = gridClient.getTopPlexerProcesses();

        println topReport
        println topTorchie
        println topPlexer

        then:
        assert statusAfterStart != null
        assert statusAfterStart.getStatus() == Status.SUCCESS

        assert topReport != null
        assert topTorchie != null
        assert topPlexer != null
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

    List<OrganizationMemberEntity> createTeamOfMembers(int memberCount) {

        List<OrganizationMemberEntity> members = new ArrayList<>()

        for (int i = 0; i < memberCount; i++) {
            //change active logged in user to a different user within same organization
            OrganizationMemberEntity teamMember =  createMemberWithinOrgAndTeam();

            members.add(teamMember)
        }

        return members
    }

    private OrganizationMemberEntity createMemberWithinOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(teamId).organizationId(org.id).memberId(member.id).save()

        return member;

    }

    void createIntention(UUID memberId, LocalDateTime time) {

        TaskEntity taskEntity = aRandom.taskEntity().forProject(projectEntity).save();

        IntentionEntity journalEntry = aRandom.intentionEntity()
                .memberId(memberId)
                .position(time)
                .projectId(projectEntity.id)
                .taskId(taskEntity.id)
                .save()

    }

    void createActivity(UUID memberId, LocalDateTime start, String filePath) {

        FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(start)
                .end(start.plusMinutes(5))
                .build()

        flowActivityEntity.setMetadataField(FlowActivityMetadataField.filePath, filePath)

        flowActivityRepository.save(flowActivityEntity)
    }

    FlowableFlowActivity generateFileActivity(UUID memberId, LocalDateTime start, String filePath) {
        FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(start)
                .end(start.plusMinutes(3))
                .build();

        flowActivityEntity.setMetadataField(FlowActivityMetadataField.filePath, filePath)

        return new FlowableFlowActivity(flowActivityEntity);

    }
}
