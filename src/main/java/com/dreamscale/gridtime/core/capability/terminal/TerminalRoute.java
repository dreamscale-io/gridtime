package com.dreamscale.gridtime.core.capability.terminal;

import com.dreamscale.gridtime.api.terminal.Command;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public abstract class TerminalRoute {

    private final Command command;

    private final String argsTemplate;

    private final UriTemplate uriTemplate;

    private Map<String, String> optionsHelpDescriptions;

    public TerminalRoute(Command command, String argsTemplate) {
        this.command = command;
        this.argsTemplate = argsTemplate;
        uriTemplate = new UriTemplate(argsTemplate);
        optionsHelpDescriptions = new LinkedHashMap<>();
    }

    public Map<String, String> extractParameters(String cmdStr) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return pathMatcher.extractUriTemplateVariables(argsTemplate, cmdStr);
    }

    protected void describeTextOption(String param, String description) {
        optionsHelpDescriptions.put(param, description);
    }

    protected void describeChoiceOption(String param, String ... choices) {
        optionsHelpDescriptions.put(param, "choose from [ "+String.join(", " , choices) + " ]");
    }

    public Object route(List<String> args) {
        String standarizedArgTemplateStr = StringUtils.join(args, " ");
        Map<String, String> params = extractParameters(standarizedArgTemplateStr);

        return route(params);
    }

    public abstract Object route(Map<String, String> params);

    /* Override if you need to add logic for multiple routes */

    protected boolean matches(List<String> args) {
        return true;
    }

    public String getArgsTemplate() {
        return argsTemplate;
    }

    public Map<String, String> getOptionsHelpDescriptions() {
        return optionsHelpDescriptions;
    }
}
