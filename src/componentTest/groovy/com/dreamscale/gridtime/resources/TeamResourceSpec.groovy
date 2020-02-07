package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.client.TeamClient
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.service.TeamService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class TeamResourceSpec extends Specification {

    @Autowired
    TeamClient teamClient

    @Autowired
    TeamService teamService

    @Autowired
    RootAccountEntity testUser

    def setup() {
        Map<String, String> keys = new HashMap<>();
        keys.put("discoveryKey", "key1")
        keys.put("key", "key2")
        keys.put("secretKey", "key3")

    }

    def "should return team with hypercore configured"() {
        given:

        RootAccountEntity account = aRandom.rootAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        testUser.setId(member.getRootAccountId())

        TeamDto team = teamService.createTeam(org.id, "myTeam")
        teamService.addMembersToTeam(org.id, team.id, Arrays.asList(member.id))

        when:
        TeamDto teamDto = teamClient.getMyPrimaryTeam()

        then:
        assert teamDto != null

    }


}
