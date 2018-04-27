package com.dreamscale.htmflow.hooks.jira

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory
import com.dreamscale.htmflow.core.hooks.jira.JiraProjectDto
import com.dreamscale.htmflow.core.hooks.jira.JiraSearchResultPage
import com.dreamscale.htmflow.core.hooks.jira.JiraUserDto
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@ComponentTest
class JiraConnectorSpec extends Specification {

    @Autowired
    JiraConnectionFactory jiraConnector


    def "should get jira projects"() {
        given:

        when:
        List<JiraProjectDto> projects = jiraConnector.getProjects()

        then:
        println projects
        assert projects.size() == 1
    }

    def "should get jira tasks"() {
        given:
        List<JiraProjectDto> projects = jiraConnector.getProjects()
        String projectId = projects.get(0).id;

        when:
        JiraSearchResultPage tasksPage = jiraConnector.getOpenTasksForProject(projectId);

        then:
        assert tasksPage.getIssues().size() > 1
    }

    def "should get jira users"() {
        given:

        when:
        List<JiraUserDto> users = jiraConnector.getUsers();

        then:
        println users
        assert users.size() == 3
    }

}
