package com.dreamscale.gridtime.core.service

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.core.hooks.talk.TalkConnection
import com.dreamscale.gridtime.core.hooks.talk.TalkConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
public class TalkIntegrationSpec extends Specification {

	@Autowired
	TalkConnectionFactory talkConnectionFactory

	//try to talk to talk!

	def setup() {

	}

	def "should join a room, and send a message"() {
		given:
		String talkRoomId = "angry_teachers-wtf"
		UUID connectionId = UUID.fromString("c0cd50cd-a9c7-48b7-889b-0ae72156bc25")
		UUID messageId = UUID.randomUUID()
		LocalDateTime now = LocalDateTime.now()
		Long nanoTime = System.nanoTime();

		when:
		TalkConnection connection = talkConnectionFactory.create(connectionId)

		//SimpleStatusDto status1 = connection.joinRoom(talkRoomId)
		SimpleStatusDto status2 = connection.sendRoomMessage(messageId, talkRoomId, now, nanoTime, "hello")

		//SimpleStatusDto status3 = connection.leaveRoom(talkRoomId)

		then:
		//what can I test?  Doesn't blow up
		//assert status1 != null
		assert status2 != null
		//assert status3 != null
	}



}
