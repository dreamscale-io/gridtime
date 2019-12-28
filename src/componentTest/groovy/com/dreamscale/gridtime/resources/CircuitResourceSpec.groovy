package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto

import com.dreamscale.gridtime.api.circuit.CreateWTFCircleInputDto
import com.dreamscale.gridtime.api.circuit.CircuitMessageDto

import com.dreamscale.gridtime.api.circuit.ScreenshotReferenceInputDto
import com.dreamscale.gridtime.api.event.NewSnippetEvent
import com.dreamscale.gridtime.client.NetworkClient
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
    NetworkClient networkClient

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
        List<CircuitMessageDto> messages = networkClient.getAllMessagesForCircleFeed(circle.id.toString());

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
        CircuitMessageDto feedMessageDto = networkClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)

        then:
        assert feedMessageDto != null
        assert feedMessageDto.getMessageType() == CircuitMessageType.CHAT
        assert feedMessageDto.getMessage() == "Here's a chat message"

        assert feedMessageDto.getMessageFrom() != null
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
        CircuitMessageDto feedMessageDto = networkClient.postScreenshotReferenceToCircleFeed(circle.id.toString(), screenshotReferenceInputDto)

        then:
        assert feedMessageDto != null
        assert feedMessageDto.getMessageType() == CircuitMessageType.SCREENSHOT
        assert feedMessageDto.getMessage() != null
        assert feedMessageDto.getFilePath() != null
        assert feedMessageDto.getFileName() != null
        assert feedMessageDto.getMessageFrom() != null
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
        CircuitMessageDto feedMessageDto = networkClient.postSnippetToActiveCircleFeed(newSnippetEvent)

        then:
        assert feedMessageDto != null
        assert feedMessageDto.getMessageType() == CircuitMessageType.SNIPPET
        assert feedMessageDto.getMessage() != null
        assert feedMessageDto.getSnippetSource() != null
        assert feedMessageDto.getSnippet() != null
        assert feedMessageDto.getMessageFrom() != null
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

        CircuitMessageDto feedMessage1 = networkClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)
        CircuitMessageDto feedMessage2 = networkClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)

        when:
        List<CircuitMessageDto> feedMessages = networkClient.getAllMessagesForCircleFeed(circle.id.toString())

        then:
        assert feedMessages != null
        assert feedMessages.size() == 3

        for (CircuitMessageDto message : feedMessages) {
            assert message.position != null
            assert message.messageFrom != null
        }

    }


}
