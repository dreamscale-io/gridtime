package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.account.SimpleStatusDto
import com.dreamscale.htmflow.api.channel.ChatMessageInputDto
import com.dreamscale.htmflow.api.circle.CircleDto
import com.dreamscale.htmflow.api.circle.CreateWTFCircleInputDto
import com.dreamscale.htmflow.api.status.Status
import com.dreamscale.htmflow.client.ChannelClient
import com.dreamscale.htmflow.client.CircleClient
import com.dreamscale.htmflow.core.domain.member.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberEntity
import com.dreamscale.htmflow.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class ChannelResourceSpec extends Specification {

    @Autowired
    ChannelClient channelClient

    @Autowired
    CircleClient circleClient

    @Autowired
    MasterAccountEntity testUser

    @Autowired
    TimeService mockTimeService

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()

    }

    def "should post a message to WTF channel"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(testUser.getId()).save()

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        when:
        CircleDto circle = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        SimpleStatusDto status = channelClient.postChatMessageToChannel(circle.getChannelId().toString(), new ChatMessageInputDto("hello world"))

        then:
        assert status != null
        assert status.getStatus() == Status.SENT

    }



}
