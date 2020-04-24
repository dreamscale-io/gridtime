package com.dreamscale.gridtime.core.service

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.circuit.ChatMessageDetailsDto
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.core.hooks.talk.TalkClientConnection
import com.dreamscale.gridtime.core.hooks.talk.TalkClientConnectionFactory
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
public class TalkIntegrationSpec extends Specification {

	@Autowired
	TalkClientConnectionFactory talkConnectionFactory

	//try to talk to talk!

	def setup() {

	}

	def "should join a room, and send a message"() {
		given:
		UUID roomId = UUID.randomUUID()
		String talkRoomName = "angry_teachers-wtf"
		UUID connectionId = UUID.fromString("c0cd50cd-a9c7-48b7-889b-0ae72156bc25")
		UUID messageId = UUID.randomUUID()
		LocalDateTime now = LocalDateTime.now()
		Long nanoTime = System.nanoTime();

		when:
		TalkClientConnection connection = talkConnectionFactory.connect()

		//SimpleStatusDto status1 = connection.joinRoom(talkRoomId)

		TalkMessageDto talkMessageDto = new TalkMessageDto(messageId, "/talk/to/room/"+talkRoomName, roomId.toString(), "/request/trace", now, nanoTime, null,
				CircuitMessageType.CHAT.getSimpleClassName(), JSONTransformer.toJson(new ChatMessageDetailsDto("hello")))

		SimpleStatusDto status2 = connection.sendRoomMessage(roomId, talkMessageDto, "joe", talkRoomName)

		//SimpleStatusDto status3 = connection.leaveRoom(talkRoomId)

		then:
		//what can I test?  Doesn't blow up
		//assert status1 != null
		assert status2 != null
		//assert status3 != null
	}



}
