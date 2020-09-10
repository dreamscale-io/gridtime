package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.AccountActivationDto
import com.dreamscale.gridtime.api.account.ActivationCodeDto
import com.dreamscale.gridtime.api.account.RootAccountCredentialsInputDto
import com.dreamscale.gridtime.api.account.UserProfileDto
import com.dreamscale.gridtime.api.circuit.ChatMessageDetailsDto
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitWithMembersDto
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.client.TalkToClient
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.capability.system.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class TalkToResourceSpec extends Specification {

    @Autowired
    LearningCircuitClient circuitClient

    @Autowired
    AccountClient accountClient

    @Autowired
    TalkToClient talkClient

    @Autowired
    RootAccountEntity testUser

    @Autowired
    GridClock gridClock

    @Autowired
    RootAccountRepository masterAccountRepository;

    OrganizationEntity org

    LocalDateTime time
    Long timeNano

    def setup() {

        time = LocalDateTime.now()
        timeNano = System.nanoTime()

        org = aRandom.organizationEntity().save()
    }

    def "should post a message to WTF room"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam()
        testUser.setId(member.getRootAccountId())

        when:
        LearningCircuitDto circuit = circuitClient.startWTF()
        //should add me to the room?

        LearningCircuitWithMembersDto details = circuitClient.getCircuitWithAllDetails(circuit.getCircuitName());

        talkClient.publishChatToRoom(circuit.getWtfTalkRoomName(), new ChatMessageInputDto("hello world"))
        talkClient.publishChatToRoom(circuit.getWtfTalkRoomName(), new ChatMessageInputDto("hello 2"))

        List<TalkMessageDto> messages = talkClient.getAllTalkMessagesFromRoom(circuit.getWtfTalkRoomName());

        then:
        assert messages != null
        assert messages.size() == 2 //status message is here too

    }

    def "should send a direct chat message"() {
        given:

        OrganizationMemberEntity member1 = createMemberWithOrgAndTeam()
        testUser.setId(member1.getRootAccountId())

        accountClient.login()

        OrganizationMemberEntity member2 = createMemberWithOrgAndTeam()

        testUser.setId(member2.getRootAccountId())

        accountClient.login()

        testUser.setId(member1.getRootAccountId())

        when:

        TalkMessageDto message = talkClient.sendDirectChatMessage(member2.getId().toString(), new ChatMessageInputDto("hello!"))

        then:
        assert message != null
        assert ((ChatMessageDetailsDto) message.getData()).getMessage() == "hello!"

    }

    private OrganizationMemberEntity createMemberWithOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(team.id).organizationId(org.id).memberId(member.id).save()

        return member;

    }
}
