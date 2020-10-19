package com.dreamscale.gridtime.api.terminal;

public enum Command {
    INVITE, SHARE, UNSHARE, VIEW, GRID, PS;

    public static Command fromString(String commandName) {
        return Command.valueOf(commandName.toUpperCase());
    }
}
