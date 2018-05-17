package com.dreamscale.htmflow.hooks.jira

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection
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
        JiraConnection jiraConnection = connect();

        when:
        List<JiraProjectDto> projects = jiraConnection.getProjects()

        then:
        println projects
        assert projects.size() == 1
    }

    def "should get jira tasks"() {
        given:
        JiraConnection jiraConnection = connect();

        List<JiraProjectDto> projects = jiraConnection.getProjects()
        String projectId = projects.get(0).id;

        when:
        JiraSearchResultPage tasksPage = jiraConnection.getOpenTasksForProject(projectId);

        then:
        assert tasksPage.getIssues().size() > 1
    }

    def "should get jira users"() {
        given:
        JiraConnection jiraConnection = connect();

        when:
        List<JiraUserDto> users = jiraConnection.getUsers();

        then:
        println users
        assert users.size() == 3
    }

    JiraConnection connect() {
       return jiraConnector.connect("dreamscale.atlassian.net", "janelle@dreamscale.io", "9KC0iM24tfXf8iKDVP2q4198");
    }

}
