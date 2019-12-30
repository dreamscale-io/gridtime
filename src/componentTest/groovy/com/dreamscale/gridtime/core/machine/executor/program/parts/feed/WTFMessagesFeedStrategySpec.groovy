package com.dreamscale.gridtime.core.machine.executor.program.parts.feed

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.core.domain.member.MasterAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Bookmark
import com.dreamscale.gridtime.core.service.LearningCircuitService
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class WTFMessagesFeedStrategySpec extends Specification{

    @Autowired
    LearningCircuitService circuitService

    @Autowired
    TimeService mockTimeService

    @Autowired
    MasterAccountEntity testUser

    @Autowired
    WTFMessagesFeedStrategy circuitMessagesFetcher

    def "should fetch batches of circle messages"() {
        given:
        mockTimeService.now() >> LocalDateTime.now()

        LocalDateTime now = mockTimeService.now();

        OrganizationEntity organization = aRandom.organizationEntity().save()
        MasterAccountEntity masterAccount = aRandom.masterAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(organization.id).masterAccountId(masterAccount.id).save();

        LearningCircuitDto circuitDto = circuitService.createNewLearningCircuit(organization.id, member.id);

        when:
        Batch batch = circuitMessagesFetcher.fetchNextBatch(member.id, new Bookmark(now.minusDays(2)), 100)

        then:
        assert batch.flowables.size() > 0
    }
}
