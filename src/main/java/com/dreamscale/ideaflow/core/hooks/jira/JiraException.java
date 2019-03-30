package com.dreamscale.ideaflow.core.hooks.jira;

public class JiraException extends RuntimeException {

    JiraException(String message, Throwable ex) {
        super(message, ex);
    }

    JiraException(String message) {
        super(message);
    }
}
