package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.ActiveUserContextDto
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.channel.ChannelMessageDto
import com.dreamscale.gridtime.api.channel.ChatMessageInputDto
import com.dreamscale.gridtime.api.circle.CircleDto
import com.dreamscale.gridtime.api.circle.CreateWTFCircleInputDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.client.ChannelClient
import com.dreamscale.gridtime.client.CircleClient
import com.dreamscale.gridtime.core.domain.member.MasterAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

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

        String channelId = circle.getChannelId().toString();

        SimpleStatusDto joinStatus = channelClient.joinChannel(channelId)

        ChannelMessageDto message1 = channelClient.postChatMessageToChannel(channelId, new ChatMessageInputDto("hello world"))
        ChannelMessageDto message2 = channelClient.postChatMessageToChannel(channelId, new ChatMessageInputDto("hello again..."))

        List<ChannelMessageDto> messages = channelClient.getAllChannelMessages(channelId);

        then:
        assert joinStatus.status == Status.VALID
        assert message1 != null
        assert message2 != null

        assert messages.size() == 2

    }

    def "should join and leave a channel"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(testUser.getId()).save()

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        when:
        CircleDto circle = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        String channelId = circle.getChannelId().toString();

        SimpleStatusDto joinStatus = channelClient.joinChannel(channelId)
        SimpleStatusDto joinAgainStatus = channelClient.joinChannel(channelId)

        List<ActiveUserContextDto> membersInChannelAfterJoin = channelClient.getActiveChannelMembers(channelId)

        SimpleStatusDto leaveStatus = channelClient.leaveChannel(channelId)
        SimpleStatusDto leaveAgainStatus = channelClient.leaveChannel(channelId)

        then:
        assert joinStatus.getStatus() == Status.VALID
        assert joinAgainStatus.getStatus() == Status.NO_ACTION

        assert membersInChannelAfterJoin.size() == 1

        assert leaveStatus.getStatus() == Status.VALID
        assert leaveAgainStatus.getStatus() == Status.NO_ACTION
    }


}
