package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.circle.CircleDto
import com.dreamscale.htmflow.core.domain.member.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberEntity
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source.Bookmark
import com.dreamscale.htmflow.core.service.CircleService
import com.dreamscale.htmflow.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class CircleMessagesFetcherSpec extends Specification{

    @Autowired
    CircleService circleService

    @Autowired
    TimeService mockTimeService

    @Autowired
    MasterAccountEntity testUser

    @Autowired
    CircleMessagesFetcher circleMessagesFetcher

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
