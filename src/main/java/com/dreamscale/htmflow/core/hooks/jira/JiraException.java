package com.dreamscale.htmflow.core.hooks.jira;

public class JiraException extends RuntimeException {

    JiraException(String message, Throwable ex) {
        super(message, ex);
    }
}
