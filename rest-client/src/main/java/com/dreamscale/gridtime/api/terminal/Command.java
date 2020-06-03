package com.dreamscale.gridtime.api.terminal;

public enum Command {
    CREATE,  INVITE, JOIN;

    public static Command fromString(String commandName) {
        return Command.valueOf(commandName.toUpperCase());
    }
}
