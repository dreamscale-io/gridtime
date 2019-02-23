package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.circle.ChatMessageInputDto
import com.dreamscale.htmflow.api.circle.CircleDto
import com.dreamscale.htmflow.api.circle.CircleInputDto
import com.dreamscale.htmflow.api.circle.CreateWTFCircleInputDto
import com.dreamscale.htmflow.api.circle.FeedMessageDto
import com.dreamscale.htmflow.api.circle.MessageType
import com.dreamscale.htmflow.client.CircleClient
import com.dreamscale.htmflow.core.domain.*
import com.dreamscale.htmflow.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class CircleResourceSpec extends Specification {

    @Autowired
    CircleClient circleClient

    @Autowired
    MasterAccountEntity testUser

    @Autowired
    TimeService mockTimeService

    def setup() {
        //mockTimeService.now() >> LocalDateTime.now()
    }

    def "should create a circle"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        when:
        CircleDto circle = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        then:
        assert circle != null
        assert circle.circleName != null
        assert circle.getPublicKey() != null

        assert circle.members != null
        assert circle.members.size() == 1

    }

    def "should post a chat message to circle feed"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        CircleDto circle = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        ChatMessageInputDto chatMessageInputDto = new ChatMessageInputDto();
        chatMessageInputDto.setChatMessage("Here's a chat message")
        chatMessageInputDto.setCircleId(circle.id)

        when:
        FeedMessageDto feedMessageDto = circleClient.postChatMessageToCircleFeed(chatMessageInputDto)

        then:
        assert feedMessageDto != null
        assert feedMessageDto.getMessageType() == MessageType.CHAT
        assert feedMessageDto.getMessage() == "Here's a chat message"

        assert feedMessageDto.getCircleMemberDto() != null
    }

    def "should retrieve all messages posted to circle feed"() {
        given:

        MasterAccountEntity account = aRandom.masterAccountEntity().save()

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        2 * mockTimeService.now() >> LocalDateTime.now().minusDays(3)
        CircleDto circle = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        ChatMessageInputDto chatMessageInputDto = new ChatMessageInputDto();
        chatMessageInputDto.setChatMessage("Here's a chat message")
        chatMessageInputDto.setCircleId(circle.id)

        1 * mockTimeService.now() >> LocalDateTime.now().minusDays(2)
        FeedMessageDto feedMessage1 = circleClient.postChatMessageToCircleFeed(chatMessageInputDto)
        1 * mockTimeService.now() >> LocalDateTime.now().minusDays(1)
        FeedMessageDto feedMessage2 = circleClient.postChatMessageToCircleFeed(chatMessageInputDto)

        when:
        List<FeedMessageDto> feedMessages = circleClient.getAllMessagesForCircleFeed(circle.id.toString())

        then:
        assert feedMessages != null
        assert feedMessages.size() == 3

        assert feedMessages.get(0).messageType == MessageType.PROBLEM_STATEMENT

        for (FeedMessageDto message : feedMessages) {
            assert message.timePosition != null
            assert message.circleMemberDto != null
        }

    }


}
