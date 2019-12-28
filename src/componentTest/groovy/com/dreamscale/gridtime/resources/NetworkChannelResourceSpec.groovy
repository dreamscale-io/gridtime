package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.account.UserContextDto
import com.dreamscale.gridtime.api.network.ChannelMessageDto
import com.dreamscale.gridtime.api.network.ChatMessageInputDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.circuit.CreateWTFCircleInputDto
import com.dreamscale.gridtime.api.network.MemberChannelsDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.client.NetworkChannelClient
import com.dreamscale.gridtime.client.NetworkClient
import com.dreamscale.gridtime.core.domain.member.MasterAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class NetworkChannelResourceSpec extends Specification {

    @Autowired
    NetworkChannelClient channelClient

    @Autowired
    NetworkClient networkClient

    @Autowired
    MasterAccountEntity testUser

    @Autowired
    TimeService mockTimeService

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()

    }

    def "should auth user on network"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(testUser.getId()).save()

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        when:
        MemberChannelsDto memberChannelsDtoBefore = networkClient.authorizeMemberToUseNetwork(member.getId().toString())

        LearningCircuitDto circle = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)
        String channelId = circle.getTalkRoomId().toString();
        SimpleStatusDto joinStatus = channelClient.joinChannel(channelId)

        MemberChannelsDto memberChannelsDtoAfter = networkClient.authorizeMemberToUseNetwork(member.getId().toString())

        then:
        assert memberChannelsDtoBefore.getUserContext().memberId == member.id
        assert memberChannelsDtoBefore.getUserContext().organizationId == member.organizationId

        assert memberChannelsDtoBefore.listeningToChannels.isEmpty()
        assert memberChannelsDtoAfter.listeningToChannels.size() == 1
    }

    def "should post a message to WTF channel"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(testUser.getId()).save()

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        when:
        LearningCircuitDto circle = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        String channelId = circle.getTalkRoomId().toString();

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
        LearningCircuitDto circle = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        String channelId = circle.getTalkRoomId().toString();

        SimpleStatusDto joinStatus = channelClient.joinChannel(channelId)
        SimpleStatusDto joinAgainStatus = channelClient.joinChannel(channelId)

        List<UserContextDto> membersInChannelAfterJoin = channelClient.getActiveChannelMembers(channelId)

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
