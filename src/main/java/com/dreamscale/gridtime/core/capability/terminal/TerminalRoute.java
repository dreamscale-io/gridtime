package com.dreamscale.gridtime.core.capability.terminal;

import com.dreamscale.gridtime.api.terminal.Command;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashMap;
import java.util.Map;


public abstract class TerminalRoute {

    private final Command command;
    private final String argPatternStr;

    private final UriTemplate uriTemplate;

    private Map<String, String> paramDescriptions;

    public TerminalRoute(Command command, String argPatternStr) {
        this.command = command;
        this.argPatternStr = argPatternStr;
        uriTemplate = new UriTemplate(argPatternStr);
        paramDescriptions = new LinkedHashMap<>();
    }

    public Map<String, String> extractParameters(String cmdStr) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return pathMatcher.extractUriTemplateVariables(argPatternStr, cmdStr);
    }

    protected void describeTextOption(String param, String description) {
        paramDescriptions.put(param, description);
    }

    protected void describeChoiceOption(String param, String ... choices) {
        paramDescriptions.put(param, "choose from [ "+String.join(", " , choices) + " ]");
    }

    public Object route(String terminalArgs) {
        Map<String, String> params = extractParameters(terminalArgs);

        return route(params);
    }

    public abstract Object route(Map<String, String> params);

    public boolean matches(String argumentStr) {
        return true;
    }
}
