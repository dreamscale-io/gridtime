package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.account.UserContextDto
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitWithMembersDto
import com.dreamscale.gridtime.api.circuit.ScreenshotReferenceInputDto
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.api.event.NewSnippetEvent

import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.circuit.CreateWTFCircleInputDto
import com.dreamscale.gridtime.api.status.Status

import com.dreamscale.gridtime.client.CircuitClient
import com.dreamscale.gridtime.client.TalkToClient
import com.dreamscale.gridtime.core.domain.member.MasterAccountEntity
import com.dreamscale.gridtime.core.domain.member.MasterAccountRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class TalkToResourceSpec extends Specification {

    @Autowired
    CircuitClient circuitClient

    @Autowired
    TalkToClient talkClient

    @Autowired
    MasterAccountEntity loggedInUser

    @Autowired
    TimeService mockTimeService

    @Autowired
    MasterAccountRepository masterAccountRepository;

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()

    }

    def "should post a message to WTF room"() {
        given:

        masterAccountRepository.save(loggedInUser);
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(loggedInUser.getId()).save()

        when:
        LearningCircuitDto circuit = circuitClient.createLearningCircuitForWTF()
        //should add me to the room?

        LearningCircuitWithMembersDto details = circuitClient.getCircuitWithAllDetails(circuit.getCircuitName());

        talkClient.publishChatToRoom(circuit.getWtfTalkRoomName(), new ChatMessageInputDto("hello world"))
        talkClient.publishChatToRoom(circuit.getWtfTalkRoomName(), new ChatMessageInputDto("hello 2"))


        List<TalkMessageDto> messages = talkClient.getAllTalkMessagesFromRoom(circuit.getWtfTalkRoomName());

        then:
        assert messages != null
        assert messages.size() == 3

    }
//
//    def "should join and leave a channel"() {
//        given:
//
//        OrganizationEntity org = aRandom.organizationEntity().save()
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(testUser.getId()).save()
//
//        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
//        circleSessionInputDto.setProblemDescription("Problem is this thing");
//
//        when:
//        LearningCircuitDto circle = circuitClient.createLearningCircuitForWTF(circleSessionInputDto)
//
//        String channelId = circle.getTalkRoomId().toString();
//
//        SimpleStatusDto joinStatus = talkClient.joinChannel(channelId)
//        SimpleStatusDto joinAgainStatus = talkClient.joinChannel(channelId)
//
//        List<UserContextDto> membersInChannelAfterJoin = talkClient.getActiveChannelMembers(channelId)
//
//        SimpleStatusDto leaveStatus = talkClient.leaveChannel(channelId)
//        SimpleStatusDto leaveAgainStatus = talkClient.leaveChannel(channelId)
//
//        then:
//        assert joinStatus.getStatus() == Status.VALID
//        assert joinAgainStatus.getStatus() == Status.NO_ACTION
//
//        assert membersInChannelAfterJoin.size() == 1
//
//        assert leaveStatus.getStatus() == Status.VALID
//        assert leaveAgainStatus.getStatus() == Status.NO_ACTION
//    }
//
//
//
//    def "should post a chat message to circle feed"() {
//        given:
//
//        OrganizationEntity org = aRandom.organizationEntity().save()
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
//        testUser.setId(member.getMasterAccountId())
//
//        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
//        circleSessionInputDto.setProblemDescription("Problem is this thing");
//
//        LearningCircuitDto circle = circuitClient.createLearningCircuitForWTF(circleSessionInputDto)
//
//        com.dreamscale.gridtime.api.circuit.ChatMessageInputDto chatMessageInputDto = new com.dreamscale.gridtime.api.circuit.ChatMessageInputDto();
//        chatMessageInputDto.setChatMessage("Here's a chat message")
//
//        when:
//        TalkMessageDto talkMessageDto = circuitClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)
//
//        then:
//        assert talkMessageDto != null
//        assert talkMessageDto.getMessageType() == CircuitMessageType.CHAT
//        assert talkMessageDto.getMessage() == "Here's a chat message"
//
//        assert talkMessageDto.getFromMember() != null
//    }
//
//    def "should post a screenshot to circle feed"() {
//        given:
//
//        OrganizationEntity org = aRandom.organizationEntity().save()
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
//        testUser.setId(member.getMasterAccountId())
//
//        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
//        circleSessionInputDto.setProblemDescription("Problem is this thing");
//
//        LearningCircuitDto circle = circuitClient.createLearningCircuitForWTF(circleSessionInputDto)
//
//        ScreenshotReferenceInputDto screenshotReferenceInputDto = new ScreenshotReferenceInputDto();
//        screenshotReferenceInputDto.setFileName("file boxName");
//        screenshotReferenceInputDto.setFilePath("/some/path/to/file")
//
//        when:
//        TalkMessageDto talkMessageDto = circuitClient.postScreenshotReferenceToCircleFeed(circle.id.toString(), screenshotReferenceInputDto)
//
//        then:
//        assert talkMessageDto != null
//        assert talkMessageDto.getMessageType() == CircuitMessageType.SCREENSHOT
//        assert talkMessageDto.getMessage() != null
//        assert talkMessageDto.getFilePath() != null
//        assert talkMessageDto.getFileName() != null
//        assert talkMessageDto.getFromMember() != null
//    }
//
//    def "should post a snippet to active circle feed"() {
//        given:
//        MasterAccountEntity account = aRandom.masterAccountEntity().save()
//        OrganizationEntity org = aRandom.organizationEntity().save()
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
//        testUser.setId(member.getMasterAccountId())
//
//        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
//        circleSessionInputDto.setProblemDescription("Problem is this thing");
//
//        LearningCircuitDto circle = circuitClient.createLearningCircuitForWTF(circleSessionInputDto)
//
//        NewSnippetEvent newSnippetEvent = new NewSnippetEvent();
//        newSnippetEvent.setSnippet("{some code}")
//        newSnippetEvent.setSource("Source.java")
//
//        when:
//        TalkMessageDto talkMessageDto = circuitClient.postSnippetToActiveCircleFeed(newSnippetEvent)
//
//        then:
//        assert talkMessageDto != null
//        assert talkMessageDto.getMessageType() == CircuitMessageType.SNIPPET
//        assert talkMessageDto.getMessage() != null
//        assert talkMessageDto.getSnippetSource() != null
//        assert talkMessageDto.getSnippet() != null
//        assert talkMessageDto.getFromMember() != null
//    }
//
//    def "should retrieve all messages posted to circle feed"() {
//        given:
//
//        MasterAccountEntity account = aRandom.masterAccountEntity().save()
//
//        OrganizationEntity org = aRandom.organizationEntity().save()
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
//        testUser.setId(member.getMasterAccountId())
//
//        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
//        circleSessionInputDto.setProblemDescription("Problem is this thing");
//
//        LearningCircuitDto circle = circuitClient.createLearningCircuitForWTF(circleSessionInputDto)
//
//        com.dreamscale.gridtime.api.circuit.ChatMessageInputDto chatMessageInputDto = new com.dreamscale.gridtime.api.circuit.ChatMessageInputDto();
//        chatMessageInputDto.setChatMessage("Here's a chat message")
//
//        TalkMessageDto talkMessage1 = circuitClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)
//        TalkMessageDto talkMessage2 = circuitClient.postChatMessageToCircleFeed(circle.id.toString(), chatMessageInputDto)
//
//        when:
//        List<TalkMessageDto> talkMessages = circuitClient.getAllMessagesForCircleFeed(circle.id.toString())
//
//        then:
//        assert talkMessages != null
//        assert talkMessages.size() == 3
//
//        for (TalkMessageDto message : talkMessages) {
//            assert message.position != null
//            assert message.fromMember != null
//        }
//
//    }
}
