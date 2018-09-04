package com.dreamscale.htmflow.core.hooks.jira;

import com.dreamscale.htmflow.core.hooks.jira.dto.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface JiraClient {

    @RequestLine("GET " + JiraPaths.API_PATH + JiraPaths.PROJECT_PATH)
    List<JiraProjectDto> getProjects();

    @RequestLine("GET " + JiraPaths.API_PATH + JiraPaths.SEARCH_PATH +
            "?jql={query}&fields={fields}")
    JiraSearchResultPage getTasks(@Param("query") String query, @Param("fields") String fields);

    @RequestLine("GET " + JiraPaths.API_PATH + JiraPaths.SEARCH_PATH +
            "?jql={query}&fields={fields}&startAt={start}&maxResults={max}")
    JiraSearchResultPage getTasksByPage(@Param("query") String query,
                                        @Param("fields") String fields,
                                        @Param("start") int startAt,
                                        @Param("max") int maxResults);

    @RequestLine("GET " + JiraPaths.API_PATH + JiraPaths.USER_PATH + JiraPaths.SEARCH_PATH +
            "?username=%25&includeActive=true&includeInactive=false")
    List<JiraUserDto> getUsers();

    @RequestLine("GET " + JiraPaths.API_PATH + JiraPaths.USER_PATH + JiraPaths.SEARCH_PATH +
            "?username={email}&includeActive=true&includeInactive=false")
    List<JiraUserDto> findUserByEmail(@Param("email") String email);

    @RequestLine("GET " + JiraPaths.API_PATH + JiraPaths.USER_PATH + "?key={user}")
    JiraUserDto getUser(@Param("user") String userKey);

    @RequestLine("POST "+ JiraPaths.API_PATH + JiraPaths.ISSUE_PATH)
    JiraTaskDto createTask(JiraNewTaskFields fields);

    @RequestLine("PUT "+ JiraPaths.API_PATH + JiraPaths.ISSUE_PATH + "/{id}" + JiraPaths.ASSIGNEE_PATH)
    JiraTaskDto updateAssignee(@Param("id") String id, JiraAssigneeUpdateDto fields);

    @RequestLine("GET "+ JiraPaths.API_PATH + JiraPaths.ISSUE_PATH + "/{id}")
    JiraTaskDto getTask(@Param("id") String id );

    @RequestLine("GET "+ JiraPaths.API_PATH + JiraPaths.ISSUE_PATH + "/{id}" + JiraPaths.TRANSITIONS_PATH)
    JiraTransitions getTransitions(@Param("id") String id);

    @RequestLine("POST "+ JiraPaths.API_PATH + JiraPaths.ISSUE_PATH + "/{id}" + JiraPaths.TRANSITIONS_PATH)
    void updateStatus(@Param("id") String id, JiraStatusPatch statusPatch);

    @RequestLine("DELETE "+ JiraPaths.API_PATH + JiraPaths.ISSUE_PATH + "/{id}")
    void deleteTask(@Param("id") String id);


//
//    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH
//            + "?from_date={fromDate}&to_date={toDate}")
//    List<IntentionOutputDto> getIntentions(@Param("fromDate") String fromDate, @Param("toDate") String toDate);
//
//
//    curl -v 'https://dreamscale.atlassian.net/rest/api/2/search?jql=project%3D10000&fields=id,key,summary' --user janelle@dreamscale.io:9KC0iM24tfXf8iKDVP2q4198 | jq
//

    //+and+status+not+in+(Done)+order+by+updated+desc&fields=id,key,summary

//    {
//        "expand": "operations,versionedRepresentations,editmeta,changelog,renderedFields",
//            "id": "10003",
//            "self": "https://dreamscale.atlassian.net/rest/api/2/issue/10003",
//            "key": "TOR-4",
//            "fields": {
//        "summary": "Create Front-End Services for Journal"
//    }
//    },

}
