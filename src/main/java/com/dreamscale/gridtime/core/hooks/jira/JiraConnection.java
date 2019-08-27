package com.dreamscale.gridtime.core.hooks.jira;

import com.dreamscale.gridtime.core.hooks.jira.dto.*;
import org.dreamscale.exception.WebApplicationException;

import java.util.ArrayList;
import java.util.List;

public class JiraConnection {

    private final JiraClient jiraClient;

    public JiraConnection(JiraClient jiraClient) {
        this.jiraClient = jiraClient;
    }

    public List<JiraProjectDto> getProjects() {
        return jiraClient.getProjects();
    }

    public JiraSearchResultPage getOpenTasksForProject(String projectId, int startAt, int maxResults) {
        System.out.println(projectId);
        String jql = "project="+projectId+" and status not in (Done) order by updated desc";
        String fields = "id,key,summary,status";

        return jiraClient.getTasksByPage(jql, fields, startAt, maxResults);
    }

    public List<JiraUserDto> getUsers() {
        List<JiraUserDto> allUsers = jiraClient.getUsers();
        List<JiraUserDto> filteredUsers = new ArrayList<>();
        for (JiraUserDto user: allUsers) {
            if (!user.getKey().startsWith("addon_")) {
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }


    public JiraUserDto getUserByEmail(String email) {
        List<JiraUserDto> usersFound = jiraClient.findUserByEmail(email);
        if (usersFound.size() > 0) {
            return usersFound.get(0);
        } else {
            return null;
        }
    }

    public JiraTaskDto createTask(JiraNewTaskDto newTask) {
        JiraNewTaskFields fields = new JiraNewTaskFields();
        fields.addSummary(newTask.getSummary());
        fields.addDescription(newTask.getDescription());
        fields.addIssueType(newTask.getIssueType());
        fields.addProject(newTask.getProjectId());

        return jiraClient.createTask(fields);
    }

    public void updateAssignee(String taskName, String jiraUserName) {
        JiraAssigneeUpdateDto jiraAssigneeUpdateDto = new JiraAssigneeUpdateDto(jiraUserName);
        jiraClient.updateAssignee(taskName, jiraAssigneeUpdateDto);
    }

    public void updateTransition(String taskName, String transitionName) {
        JiraTransitions transitions = jiraClient.getTransitions(taskName);

        JiraTransition selectedTransition = null;

        for (JiraTransition transition : transitions.getTransitions() ) {
            JiraStatus status = transition.getTo();
            if (transitionName != null && transitionName.equalsIgnoreCase(status.getName())) {
                selectedTransition = transition;
                break;
            }
        }

        if (selectedTransition == null) {
            throw new JiraException("Unable to find transition for status: "+transitionName);
        }

        JiraStatusPatch statusPatch = new JiraStatusPatch(selectedTransition.getId(), "Transitioned state to "+transitionName);
        jiraClient.updateStatus(taskName, statusPatch);

    }

    public void validate() {
        try {
            List<JiraProjectDto> projects = jiraClient.getProjects();
        } catch (WebApplicationException ex) {
            throw new JiraException("["+ex.getStatusCode() + "]: Unable to retrieve data from Jira, "+ex.getMessage(), ex);
        }
    }

    public JiraTaskDto getTask(String taskName) {
        return jiraClient.getTask(taskName);
    }

    public JiraTransitions getTransitions(String taskName) {
        return jiraClient.getTransitions(taskName);
    }

    public JiraUserDto getUserByKey(String userKey) {
        return jiraClient.getUser(userKey);
    }

    public void deleteTask(String taskKey) {
        jiraClient.deleteTask(taskKey);
    }
}
