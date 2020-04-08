package com.dreamscale.gridtime.core.service

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.core.capability.integration.EmailCapability
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
public class EmailCapabilitySpec extends Specification {

	@Autowired
	EmailCapability emailCapability

	@Autowired
	RootAccountEntity loggedInUser

	def setup() {

	}

	def "should send a validation email"() {
		given:

		when:
		SimpleStatusDto emailSendStatus = emailCapability.sendDownloadAndActivationEmail("arty@dreamscale.io", "AAATOKEN")

		then:
		assert emailSendStatus != null
		assert emailSendStatus.getStatus() == Status.VALID;
	}




}
