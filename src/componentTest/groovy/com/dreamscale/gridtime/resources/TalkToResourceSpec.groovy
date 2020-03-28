package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitWithMembersDto
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.client.TalkToClient
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class TalkToResourceSpec extends Specification {

    @Autowired
    LearningCircuitClient circuitClient

    @Autowired
    TalkToClient talkClient

    @Autowired
    RootAccountEntity loggedInUser

    @Autowired
    GridClock mockTimeService

    @Autowired
    RootAccountRepository masterAccountRepository;

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()

    }

    def "should post a message to WTF room"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam()
        loggedInUser.setId(member.getRootAccountId())

        when:
        LearningCircuitDto circuit = circuitClient.startWTF()
        //should add me to the room?

        LearningCircuitWithMembersDto details = circuitClient.getCircuitWithAllDetails(circuit.getCircuitName());

        talkClient.publishChatToRoom(circuit.getWtfTalkRoomName(), new ChatMessageInputDto("hello world"))
        talkClient.publishChatToRoom(circuit.getWtfTalkRoomName(), new ChatMessageInputDto("hello 2"))

        List<TalkMessageDto> messages = talkClient.getAllTalkMessagesFromRoom(circuit.getWtfTalkRoomName());

        then:
        assert messages != null
        assert messages.size() == 2

    }

    private OrganizationMemberEntity createMemberWithOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationEntity org = aRandom.organizationEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(team.id).organizationId(org.id).memberId(member.id).save()

        return member;

    }
//
//    def "should join and leave a channel"() {
//        given:
//
//        OrganizationEntity org = aRandom.organizationEntity().save()
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(testUser.getId()).save()
//
//        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
//        circleSessionInputDto.setProblemDescription("Problem is this thing");
//
//        when:
//        LearningCircuitDto circle = circuitClient.startLearningCircuitForWTF(circleSessionInputDto)
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
//        testUser.setId(member.getRootAccountId())
//
//        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
//        circleSessionInputDto.setProblemDescription("Problem is this thing");
//
//        LearningCircuitDto circle = circuitClient.startLearningCircuitForWTF(circleSessionInputDto)
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
//        testUser.setId(member.getRootAccountId())
//
//        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
//        circleSessionInputDto.setProblemDescription("Problem is this thing");
//
//        LearningCircuitDto circle = circuitClient.startLearningCircuitForWTF(circleSessionInputDto)
//
//        ScreenshotReferenceInputDto screenshotReferenceInputDto = new ScreenshotReferenceInputDto();
//        screenshotReferenceInputDto.setFileName("file boxName");
//        screenshotReferenceInputDto.setFileUri("/some/path/to/file")
//
//        when:
//        TalkMessageDto talkMessageDto = circuitClient.postScreenshotReferenceToCircleFeed(circle.id.toString(), screenshotReferenceInputDto)
//
//        then:
//        assert talkMessageDto != null
//        assert talkMessageDto.getMessageType() == CircuitMessageType.SCREENSHOT
//        assert talkMessageDto.getMessage() != null
//        assert talkMessageDto.getFileUri() != null
//        assert talkMessageDto.getFileName() != null
//        assert talkMessageDto.getFromMember() != null
//    }
//
//    def "should post a snippet to active circle feed"() {
//        given:
//        RootAccountEntity account = aRandom.rootAccountEntity().save()
//        OrganizationEntity org = aRandom.organizationEntity().save()
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
//        testUser.setId(member.getRootAccountId())
//
//        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
//        circleSessionInputDto.setProblemDescription("Problem is this thing");
//
//        LearningCircuitDto circle = circuitClient.startLearningCircuitForWTF(circleSessionInputDto)
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
//        RootAccountEntity account = aRandom.rootAccountEntity().save()
//
//        OrganizationEntity org = aRandom.organizationEntity().save()
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
//        testUser.setId(member.getRootAccountId())
//
//        CreateWTFCircleInputDto circleSessionInputDto = new CreateWTFCircleInputDto();
//        circleSessionInputDto.setProblemDescription("Problem is this thing");
//
//        LearningCircuitDto circle = circuitClient.startLearningCircuitForWTF(circleSessionInputDto)
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
