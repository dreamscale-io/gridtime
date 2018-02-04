package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.account.ActivationTokenDto
import com.dreamscale.htmflow.api.account.AccountActivationDto
import com.dreamscale.htmflow.api.account.HeartbeatDto
import com.dreamscale.htmflow.api.account.SimpleStatusDto
import com.dreamscale.htmflow.client.AccountClient
import com.dreamscale.htmflow.core.domain.ProjectRepository
import com.dreamscale.htmflow.core.domain.TaskRepository
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@ComponentTest
class AccountResourceSpec extends Specification {

    @Autowired
    AccountClient accountClient
    @Autowired
    ProjectRepository projectRepository
    @Autowired
    TaskRepository taskRepository

    def setup() {
        projectRepository.deleteAll()
        taskRepository.deleteAll()
    }

    def "should activate account"() {
        given:
        ActivationTokenDto accountKey = new ActivationTokenDto();

        when:
        AccountActivationDto statusDto = accountClient.activate(accountKey)

        then:
        assert statusDto != null
    }

    def "should login"() {
        given:

        when:
        SimpleStatusDto statusDto = accountClient.login()

        then:
        assert statusDto != null
    }

    def "should logout"() {
        given:

        when:
        SimpleStatusDto statusDto = accountClient.logout()

        then:
        assert statusDto != null
    }

    def "should update heartbeat"() {
        given:
        HeartbeatDto heartbeatDto = new HeartbeatDto();

        when:
        SimpleStatusDto statusDto = accountClient.heartbeat(heartbeatDto)

        then:
        assert statusDto != null
    }


}
