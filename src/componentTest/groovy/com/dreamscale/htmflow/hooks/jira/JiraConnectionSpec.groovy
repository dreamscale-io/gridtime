package com.dreamscale.htmflow.hooks.jira

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraNewTaskDto
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraProjectDto
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraTaskDto
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraTransitions
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraUserDto
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification

@ComponentTest
class JiraConnectionSpec extends Specification {

    @Autowired
    JiraConnectionFactory jiraConnectionFactory


    def "should get jira projects"() {
        given:
        JiraConnection jiraConnection = connect();

        when:
        List<JiraProjectDto> projects = jiraConnection.getProjects()

        then:
        println projects
        assert projects.size() == 5
    }

    def "should get jira tasks"() {
        given:
        JiraConnection jiraConnection = connect();

        List<JiraProjectDto> projects = jiraConnection.getProjects()
        String projectId = projects.get(0).id;

        when:
        List<JiraTaskDto> taskDtos = jiraConnection.getOpenTasksForProject(projectId);

        then:
        assert taskDtos.size() > 1
    }

    def "should get jira users"() {
        given:
        JiraConnection jiraConnection = connect();

        when:
        List<JiraUserDto> users = jiraConnection.getUsers();

        then:
        println users
        assert users.size() == 6
    }

    def "get transitions for a task"() {
        given:
        JiraConnection jiraConnection = connect();

        List<JiraProjectDto> projects = jiraConnection.getProjects()
        String projectId = projects.get(0).id;

        List<JiraTaskDto> taskDtos = jiraConnection.getOpenTasksForProject(projectId);

        when:
        JiraTransitions transitions = jiraConnection.getTransitions(taskDtos.get(0).getKey());

        then:
        assert transitions.transitions.size() > 1
    }


    @Ignore
    def "save new jira task"() {
        given:
        JiraConnection jiraConnection = connect();

        List<JiraProjectDto> projects = jiraConnection.getProjects()
        String projectId = projects.get(0).id;

        JiraNewTaskDto taskDto = new JiraNewTaskDto("summary", "description", projectId, "Task");

        when:
        JiraTaskDto task = jiraConnection.createTask(taskDto)

        then:
        println task
        assert task.key != null
        assert task.id != null
    }

    @Ignore
    def "transition state of jira task to in progress"() {
        given:
        JiraConnection jiraConnection = connect();

        List<JiraProjectDto> projects = jiraConnection.getProjects()
        String projectId = projects.get(0).id;

        JiraNewTaskDto taskDto = new JiraNewTaskDto("summary", "description", projectId, "Task");
        JiraTaskDto task = jiraConnection.createTask(taskDto)

        when:
        jiraConnection.updateTransition(task.getKey(), "In Progress");

        then:
        println task
        assert task.key != null
        assert task.id != null
    }

    @Ignore
    def "assign jira task to user"() {
        given:
        JiraConnection jiraConnection = connect();

        List<JiraProjectDto> projects = jiraConnection.getProjects()
        String projectId = projects.get(0).id;

        JiraNewTaskDto taskDto = new JiraNewTaskDto("summary", "description", projectId, "Task");

        JiraTaskDto task = jiraConnection.createTask(taskDto)

        JiraUserDto user = jiraConnection.getUsers().get(0);

        when:
        jiraConnection.updateAssignee(task.getKey(), user.getName());
        JiraTaskDto updatedTask = jiraConnection.getTask(task.getKey());

        then:
        println updatedTask
        assert updatedTask.key != null
        assert updatedTask.id != null
        assert updatedTask.fields.get("assignee") != null;
    }

    JiraConnection connect() {
       return jiraConnectionFactory.connect("dreamscale.atlassian.net", "janelle@dreamscale.io", "9KC0iM24tfXf8iKDVP2q4198");
    }

}
