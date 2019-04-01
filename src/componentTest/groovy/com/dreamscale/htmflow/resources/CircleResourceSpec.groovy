package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.circle.ChatMessageInputDto
import com.dreamscale.htmflow.api.circle.CircleDto
import com.dreamscale.htmflow.api.circle.CircleKeyDto
import com.dreamscale.htmflow.api.circle.CreateWTFCircleInputDto
import com.dreamscale.htmflow.api.circle.FeedMessageDto
import com.dreamscale.htmflow.api.circle.MessageType
import com.dreamscale.htmflow.api.circle.ScreenshotReferenceInputDto
import com.dreamscale.htmflow.api.event.NewSnippetEvent
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
        assert circle.problemDescription != null
        assert circle.getPublicKey() != null

        assert circle.members != null
        assert circle.members.size() == 1

    }

    def "should return all open circles"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        CircleDto circle1 = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)
        CircleDto circle2 = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        when:
        List<CircleDto> circles = circleClient.getAllOpenCircles()

        then:
        assert circles != null
        assert circles.size() == 2
    }

    def "should return active circle"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        CircleDto circle1 = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        when:
        CircleDto activeCircle = circleClient.getActiveCircle()

        then:
        assert activeCircle != null
        assert activeCircle.id == circle1.id
    }

    def "should return circle key"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        CircleDto circle1 = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        when:
        CircleKeyDto circleKeyDto = circleClient.getCircleKey(circle1.id.toString())

        then:
        assert circleKeyDto != null
        assert circleKeyDto.privateKey != null
    }


    def "should return all shelved do it later circles"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        CircleDto circle1 = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)
        CircleDto circle2 = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        when:
        circleClient.shelveCircleWithDoItLater(circle1.id.toString())
        List<CircleDto> circles = circleClient.getAllDoItLaterCircles()

        then:
        assert circles != null
        assert circles.size() == 1
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

        when:

        CircleDto circleDto = circleClient.closeCircle(circle.id.toString());
        List<FeedMessageDto> messages = circleClient.getAllMessagesForCircleFeed(circle.id.toString());

        then:
        assert circleDto != null
        assert messages != null
        assert messages.size() == 2
    }


    def "should shelf a circle with do it later"() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        CircleDto circle = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        when:

        CircleDto circleDto = circleClient.shelveCircleWithDoItLater(circle.id.toString());

        then:
        assert circleDto != null
        assert circleDto.onShelf == true
    }

    def "should resume a circle from do it later"() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        CircleDto circle = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        CircleDto circleShelved = circleClient.shelveCircleWithDoItLater(circle.id.toString());

        when:

        CircleDto resumedCircle = circleClient.resumeAnExistingCircleFromDoItLaterShelf(circle.id.toString());

        then:
        assert resumedCircle != null
        assert resumedCircle.onShelf == false
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

        when:
        FeedMessageDto feedMessageDto = circleClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)

        then:
        assert feedMessageDto != null
        assert feedMessageDto.getMessageType() == MessageType.CHAT
        assert feedMessageDto.getMessage() == "Here's a chat message"

        assert feedMessageDto.getCircleMemberDto() != null
    }

    def "should post a screenshot to circle feed"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        CircleDto circle = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        ScreenshotReferenceInputDto screenshotReferenceInputDto = new ScreenshotReferenceInputDto();
        screenshotReferenceInputDto.setFileName("file placeName");
        screenshotReferenceInputDto.setFilePath("/some/path/to/file")

        when:
        FeedMessageDto feedMessageDto = circleClient.postScreenshotReferenceToCircleFeed(circle.id.toString(), screenshotReferenceInputDto)

        then:
        assert feedMessageDto != null
        assert feedMessageDto.getMessageType() == MessageType.SCREENSHOT
        assert feedMessageDto.getMessage() != null
        assert feedMessageDto.getFilePath() != null
        assert feedMessageDto.getFileName() != null
        assert feedMessageDto.getCircleMemberDto() != null
    }

    def "should post a snippet to active circle feed"() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        CircleDto circle = circleClient.createNewAdhocWTFCircle(circleSessionInputDto)

        NewSnippetEvent newSnippetEvent = new NewSnippetEvent();
        newSnippetEvent.setSnippet("{some code}")
        newSnippetEvent.setSource("Source.java")

        when:
        FeedMessageDto feedMessageDto = circleClient.postSnippetToActiveCircleFeed(newSnippetEvent)

        then:
        assert feedMessageDto != null
        assert feedMessageDto.getMessageType() == MessageType.SNIPPET
        assert feedMessageDto.getMessage() != null
        assert feedMessageDto.getSnippetSource() != null
        assert feedMessageDto.getSnippet() != null
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

        FeedMessageDto feedMessage1 = circleClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)
        FeedMessageDto feedMessage2 = circleClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)

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
