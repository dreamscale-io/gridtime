package com.dreamscale.gridtime.core.machine.executor.program.parts.feed

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circle.CircleDto
import com.dreamscale.gridtime.core.domain.member.MasterAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Bookmark
import com.dreamscale.gridtime.core.service.CircleService
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class WTFMessagesFeedStrategySpec extends Specification{

    @Autowired
    CircleService circleService

    @Autowired
    TimeService mockTimeService

    @Autowired
    MasterAccountEntity testUser

    @Autowired
    WTFMessagesFeedStrategy circleMessagesFetcher

    def "should fetch batches of circle messages"() {
        given:
        mockTimeService.now() >> LocalDateTime.now()

        LocalDateTime now = mockTimeService.now();

        OrganizationEntity organization = aRandom.organizationEntity().save()
        MasterAccountEntity masterAccount = aRandom.masterAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(organization.id).masterAccountId(masterAccount.id).save();

        CircleDto circle = circleService.createNewAdhocCircle(organization.id, member.id, "problem");

        when:
        Batch batch = circleMessagesFetcher.fetchNextBatch(member.id, new Bookmark(now.minusDays(2)), 100)

        then:
        assert batch.flowables.size() > 0
    }
}
