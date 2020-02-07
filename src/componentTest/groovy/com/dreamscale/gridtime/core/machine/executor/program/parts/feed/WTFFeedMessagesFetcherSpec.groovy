package com.dreamscale.gridtime.core.machine.executor.program.parts.feed

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Bookmark
import com.dreamscale.gridtime.core.service.CircuitOperator
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class WTFFeedMessagesFetcherSpec extends Specification{

    @Autowired
    CircuitOperator circuitService

    @Autowired
    TimeService mockTimeService

    @Autowired
    RootAccountEntity testUser

    @Autowired
    WTFFeedMessagesFetcher circuitMessagesFetcher

    def "should fetch batches of circle messages"() {
        given:
        mockTimeService.now() >> LocalDateTime.now()

        LocalDateTime now = mockTimeService.now();

        OrganizationEntity organization = aRandom.organizationEntity().save()
        RootAccountEntity masterAccount = aRandom.rootAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(organization.id).rootAccountId(masterAccount.id).save();

        LearningCircuitDto circuitDto = circuitService.createNewLearningCircuit(organization.id, member.id);

        when:
        Batch batch = circuitMessagesFetcher.fetchNextBatch(member.id, new Bookmark(now.minusDays(2)), 100)

        then:
        assert batch.flowables.size() > 0
    }
}
