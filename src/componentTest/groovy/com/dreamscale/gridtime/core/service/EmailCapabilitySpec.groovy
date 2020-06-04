package com.dreamscale.gridtime.core.service

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.core.capability.external.EmailCapability
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification

@ComponentTest
public class EmailCapabilitySpec extends Specification {

	@Autowired
	EmailCapability emailCapability

	@Autowired
	RootAccountEntity loggedInUser

	def setup() {

	}

	//This is autowiring a mock now for standard testing purposes.  Disable the mock, to actually test integration.

	//TODO need to setup another test config that doesn't use mocks

	@Ignore
	def "should send a validation email"() {
		given:

		when:
		SimpleStatusDto emailSendStatus = emailCapability.sendDownloadAndActivationEmail("arty@dreamscale.io", "AAATOKEN")

		then:
		assert emailSendStatus != null
		assert emailSendStatus.getStatus() == Status.VALID;
	}




}
