package com.dreamscale.gridtime.core.capability.terminal;

import com.dreamscale.gridtime.api.terminal.Command;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dreamscale.exception.BadRequestException;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.UriTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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

        Map<String, String> params = null;
        try {
            params = pathMatcher.extractUriTemplateVariables(argsTemplate, cmdStr);
        } catch (IllegalStateException ex) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_COMMAND_PARAMETERS, "Invalid parameters. "+ex.getMessage());
        }

        return params;
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

    protected boolean matches(Command inputCommand, List<String> args) {
        if (inputCommand.equals(command)) {

            String standarizedArgTemplateStr = StringUtils.join(args, " ");
            return uriTemplate.matches(standarizedArgTemplateStr);
        }
        return false;
    }

    public String getArgsTemplate() {
        return argsTemplate;
    }

    public Map<String, String> getOptionsHelpDescriptions() {
        return optionsHelpDescriptions;
    }

    public Command getCommand() {
        return command;
    }
}
