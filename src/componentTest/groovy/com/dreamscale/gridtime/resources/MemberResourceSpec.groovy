package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.organization.TeamMemberDto
import com.dreamscale.gridtime.client.MemberClient
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.capability.membership.TeamCapability
import com.dreamscale.gridtime.core.capability.system.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class MemberResourceSpec extends Specification {

    @Autowired
    MemberClient memberClient

    @Autowired
    RootAccountEntity testUser

    @Autowired
    TeamCapability teamCapability

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
        TeamMemberDto memberWorkStatusDto = memberClient.getMe()

        then:
        assert memberWorkStatusDto != null
        assert memberWorkStatusDto.getUsername() != null;

    }

}
