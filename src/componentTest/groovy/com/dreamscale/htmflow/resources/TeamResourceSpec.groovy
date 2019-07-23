package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.team.TeamDto
import com.dreamscale.htmflow.client.TeamClient
import com.dreamscale.htmflow.core.domain.member.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberEntity

import com.dreamscale.htmflow.core.service.TeamService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class TeamResourceSpec extends Specification {

    @Autowired
    TeamClient teamClient

    @Autowired
    TeamService teamService

    @Autowired
    MasterAccountEntity testUser

    def setup() {
        Map<String, String> keys = new HashMap<>();
        keys.put("discoveryKey", "key1")
        keys.put("key", "key2")
        keys.put("secretKey", "key3")

    }

    def "should return team with hypercore configured"() {
        given:

        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        testUser.setId(member.getMasterAccountId())

        TeamDto team = teamService.createTeam(org.id, "myTeam")
        teamService.addMembersToTeam(org.id, team.id, Arrays.asList(member.id))

        when:
        TeamDto teamDto = teamClient.getMyPrimaryTeam()

        then:
        assert teamDto != null

    }


}
