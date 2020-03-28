package com.dreamscale.gridtime.core.machine.executor.program.parts.feed

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Bookmark
import com.dreamscale.gridtime.core.capability.operator.LearningCircuitOperator
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class WTFFeedMessagesFetcherSpec extends Specification{

    @Autowired
    LearningCircuitOperator circuitOperator

    @Autowired
    GridClock mockTimeService

    @Autowired
    RootAccountEntity testUser

    @Autowired
    WTFFeedMessagesFetcher circuitMessagesFetcher

    def "should fetch batches of circle messages"() {
        given:
        mockTimeService.now() >> LocalDateTime.now()

        LocalDateTime now = mockTimeService.now();

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();

        LearningCircuitDto circuitDto = circuitOperator.startWTF(member.getOrganizationId(), member.id);

        when:
        Batch batch = circuitMessagesFetcher.fetchNextBatch(member.id, new Bookmark(now.minusDays(2)), 100)

        then:
        assert batch.flowables.size() > 0
    }

    private OrganizationMemberEntity createMemberWithOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationEntity org = aRandom.organizationEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(team.id).organizationId(org.id).memberId(member.id).save()

        return member;

    }

}
