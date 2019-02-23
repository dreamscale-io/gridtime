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
        mockTimeService.now() >> LocalDateTime.now()
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

    def "should close a circle"() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        CircleDto circle = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        CircleInputDto circleInputDto = new CircleInputDto();
        circleInputDto.setCircleId(circle.id);
        when:

        circleClient.closeCircle(circleInputDto);
        List<FeedMessageDto> messages = circleClient.getAllMessagesForCircleFeed(circle.id.toString());

        then:
        assert messages != null
        assert messages.size() == 2
        assert messages.get(1).message == "Circle closed."
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

        CircleDto circle = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        ChatMessageInputDto chatMessageInputDto = new ChatMessageInputDto();
        chatMessageInputDto.setChatMessage("Here's a chat message")
        chatMessageInputDto.setCircleId(circle.id)

        FeedMessageDto feedMessage1 = circleClient.postChatMessageToCircleFeed(chatMessageInputDto)
        FeedMessageDto feedMessage2 = circleClient.postChatMessageToCircleFeed(chatMessageInputDto)

        when:
        List<FeedMessageDto> feedMessages = circleClient.getAllMessagesForCircleFeed(circle.id.toString())

        then:
        assert feedMessages != null
        assert feedMessages.size() == 3

        for (FeedMessageDto message : feedMessages) {
            assert message.timePosition != null
            assert message.circleMemberDto != null
        }

    }


}
