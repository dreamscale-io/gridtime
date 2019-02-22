package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.circle.ChatMessageInputDto
import com.dreamscale.htmflow.api.circle.CircleDto
import com.dreamscale.htmflow.api.circle.CreateWTFCircleInputDto
import com.dreamscale.htmflow.api.circle.FeedMessageDto
import com.dreamscale.htmflow.api.circle.MessageType
import com.dreamscale.htmflow.client.CircleClient
import com.dreamscale.htmflow.core.domain.*
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class CircleResourceSpec extends Specification {

    @Autowired
    CircleClient circleClient

    @Autowired
    MasterAccountEntity testUser


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

}
