package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.organization.MemberWorkStatusDto
import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.client.MemberStatusClient
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.capability.directory.TeamMembershipCapability
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class MemberStatusResourceSpec extends Specification {

    @Autowired
    MemberStatusClient memberStatusClient

    @Autowired
    RootAccountEntity testUser

    @Autowired
    TeamMembershipCapability teamService

    @Autowired
    GridClock mockTimeService

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()
    }

    def "should return status for current user"() {
        given:

        RootAccountEntity account = aRandom.rootAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        testUser.setId(member.getRootAccountId())

        when:
        MemberWorkStatusDto memberWorkStatusDto = memberStatusClient.getMyCurrentStatus()

        then:
        assert memberWorkStatusDto != null
        assert memberWorkStatusDto.getUsername() != null;

    }

    def "should return status for current team"() {
        given:

        RootAccountEntity account = aRandom.rootAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        testUser.setId(member.getRootAccountId())

        TeamDto team = teamService.createTeam(org.id, "myTeam")
        teamService.addMembersToTeam(org.id, team.id, Arrays.asList(member.id))

        when:
        List<MemberWorkStatusDto> members = memberStatusClient.getStatusOfMeAndMyTeam()

        then:
        assert members != null
        assert members.size() > 0

        assert members.get(0).getUsername() != null;
    }

}
