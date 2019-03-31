package com.dreamscale.ideaflow.resources

import com.dreamscale.ideaflow.ComponentTest
import com.dreamscale.ideaflow.api.organization.MemberWorkStatusDto
import com.dreamscale.ideaflow.api.team.TeamDto
import com.dreamscale.ideaflow.client.MemberStatusClient
import com.dreamscale.ideaflow.core.domain.MasterAccountEntity
import com.dreamscale.ideaflow.core.domain.OrganizationEntity
import com.dreamscale.ideaflow.core.domain.OrganizationMemberEntity
import com.dreamscale.ideaflow.core.service.TeamService
import com.dreamscale.ideaflow.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom

@ComponentTest
class MemberStatusResourceSpec extends Specification {

    @Autowired
    MemberStatusClient memberStatusClient

    @Autowired
    MasterAccountEntity testUser

    @Autowired
    TeamService teamService

    @Autowired
    TimeService mockTimeService

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()
    }

    def "should return status for current user"() {
        given:

        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        testUser.setId(member.getMasterAccountId())

        when:
        MemberWorkStatusDto memberWorkStatusDto = memberStatusClient.getMyCurrentStatus()

        then:
        assert memberWorkStatusDto != null

    }

    def "should return status for current team"() {
        given:

        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        testUser.setId(member.getMasterAccountId())

        TeamDto team = teamService.createTeam(org.id, "myTeam")
        teamService.addMembersToTeam(org.id, team.id, Arrays.asList(member.id))

        when:
        List<MemberWorkStatusDto> members = memberStatusClient.getStatusOfMeAndMyTeam()

        then:
        assert members != null
        assert members.size() > 0

    }

}