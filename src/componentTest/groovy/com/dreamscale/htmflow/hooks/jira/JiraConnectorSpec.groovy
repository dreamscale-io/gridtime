package com.dreamscale.htmflow.hooks.jira

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.account.AccountActivationDto
import com.dreamscale.htmflow.api.account.ActivationTokenDto
import com.dreamscale.htmflow.api.account.HeartbeatDto
import com.dreamscale.htmflow.api.account.SimpleStatusDto
import com.dreamscale.htmflow.client.AccountClient
import com.dreamscale.htmflow.core.domain.ProjectRepository
import com.dreamscale.htmflow.core.domain.TaskRepository
import com.dreamscale.htmflow.core.hooks.jira.JiraConnector
import com.dreamscale.htmflow.core.hooks.jira.JiraProjectDto
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@ComponentTest
class JiraConnectorSpec extends Specification {

    @Autowired
    JiraConnector jiraConnector


    def "should get jira projects"() {
        given:

        when:
        List<JiraProjectDto> projects = jiraConnector.getProjects()

        then:
        println projects
        assert projects.size() == 1
    }


}
