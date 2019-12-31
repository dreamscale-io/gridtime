package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto

import com.dreamscale.gridtime.api.circuit.CreateWTFCircleInputDto

import com.dreamscale.gridtime.api.circuit.ScreenshotReferenceInputDto
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.api.event.NewSnippetEvent
import com.dreamscale.gridtime.client.CircuitTalkClient
import com.dreamscale.gridtime.core.domain.member.MasterAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity

import com.dreamscale.gridtime.core.service.TeamService
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class CircuitResourceSpec extends Specification {

    @Autowired
    CircuitTalkClient networkClient

    @Autowired
    MasterAccountEntity testUser

    @Autowired
    TeamService teamService

    @Autowired
    TimeService mockTimeService

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()

        Map<String, String> keys = new HashMap<>();
        keys.put("discoveryKey", "key1")
        keys.put("key", "key2")
        keys.put("secretKey", "key3")

    }


    def "should create a circle"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        when:
        LearningCircuitDto circle = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        then:
        assert circle != null
        assert circle.getCircuitName != null
        assert circle.problemDescription != null

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

        LearningCircuitDto circle1 = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)
        LearningCircuitDto circle2 = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        when:
        List<LearningCircuitDto> circles = networkClient.getAllOpenCircles()

        then:
        assert circles != null
        assert circles.size() == 2
        assert circle1.getTalkRoomId() != null
        assert circles.get(0).getTalkRoomId() != null
    }

    def "should return active circle"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        LearningCircuitDto circle1 = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        when:
        LearningCircuitDto activeCircle = networkClient.getActiveCircle()

        then:
        assert activeCircle != null
        assert activeCircle.id == circle1.id
        assert activeCircle.getTalkRoomId() != null
    }



    def "should return all shelved do it later circles"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        LearningCircuitDto circle1 = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)
        LearningCircuitDto circle2 = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        when:
        networkClient.shelveCircleWithDoItLater(circle1.id.toString())
        List<LearningCircuitDto> circles = networkClient.getAllDoItLaterCircles()

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

        LearningCircuitDto circle = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        when:

        LearningCircuitDto circleDto = networkClient.closeCircle(circle.id.toString());
        List<TalkMessageDto> messages = networkClient.getAllMessagesForCircleFeed(circle.id.toString());

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

        LearningCircuitDto circle = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        when:

        LearningCircuitDto circleDto = networkClient.shelveCircleWithDoItLater(circle.id.toString());

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

        LearningCircuitDto circle = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        LearningCircuitDto circleShelved = networkClient.shelveCircleWithDoItLater(circle.id.toString());

        when:

        LearningCircuitDto resumedCircle = networkClient.resumeAnExistingCircleFromDoItLaterShelf(circle.id.toString());

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

        LearningCircuitDto circle = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        ChatMessageInputDto chatMessageInputDto = new ChatMessageInputDto();
        chatMessageInputDto.setChatMessage("Here's a chat message")

        when:
        TalkMessageDto talkMessageDto = networkClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)

        then:
        assert talkMessageDto != null
        assert talkMessageDto.getMessageType() == CircuitMessageType.CHAT
        assert talkMessageDto.getMessage() == "Here's a chat message"

        assert talkMessageDto.getFromMember() != null
    }

    def "should post a screenshot to circle feed"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        LearningCircuitDto circle = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        ScreenshotReferenceInputDto screenshotReferenceInputDto = new ScreenshotReferenceInputDto();
        screenshotReferenceInputDto.setFileName("file boxName");
        screenshotReferenceInputDto.setFilePath("/some/path/to/file")

        when:
        TalkMessageDto talkMessageDto = networkClient.postScreenshotReferenceToCircleFeed(circle.id.toString(), screenshotReferenceInputDto)

        then:
        assert talkMessageDto != null
        assert talkMessageDto.getMessageType() == CircuitMessageType.SCREENSHOT
        assert talkMessageDto.getMessage() != null
        assert talkMessageDto.getFilePath() != null
        assert talkMessageDto.getFileName() != null
        assert talkMessageDto.getFromMember() != null
    }

    def "should post a snippet to active circle feed"() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        LearningCircuitDto circle = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        NewSnippetEvent newSnippetEvent = new NewSnippetEvent();
        newSnippetEvent.setSnippet("{some code}")
        newSnippetEvent.setSource("Source.java")

        when:
        TalkMessageDto talkMessageDto = networkClient.postSnippetToActiveCircleFeed(newSnippetEvent)

        then:
        assert talkMessageDto != null
        assert talkMessageDto.getMessageType() == CircuitMessageType.SNIPPET
        assert talkMessageDto.getMessage() != null
        assert talkMessageDto.getSnippetSource() != null
        assert talkMessageDto.getSnippet() != null
        assert talkMessageDto.getFromMember() != null
    }

    def "should retrieve all messages posted to circle feed"() {
        given:

        MasterAccountEntity account = aRandom.masterAccountEntity().save()

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        testUser.setId(member.getMasterAccountId())

        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
        circleSessionInputDto.setProblemDescription("Problem is this thing");

        LearningCircuitDto circle = networkClient.createNewAdhocWTFCircle(circleSessionInputDto)

        ChatMessageInputDto chatMessageInputDto = new ChatMessageInputDto();
        chatMessageInputDto.setChatMessage("Here's a chat message")

        TalkMessageDto talkMessage1 = networkClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)
        TalkMessageDto talkMessage2 = networkClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)

        when:
        List<TalkMessageDto> talkMessages = networkClient.getAllMessagesForCircleFeed(circle.id.toString())

        then:
        assert talkMessages != null
        assert talkMessages.size() == 3

        for (TalkMessageDto message : talkMessages) {
            assert message.position != null
            assert message.fromMember != null
        }

    }


}
