package com.dreamscale.gridtime.api.terminal;

public enum Command {
    INVITE, SHARE, UNSHARE, VIEW, GRID, PS, ERROR, PURGE, SELECT, TARGET, GT, GOTO, ZOOM, PAN, LOOK, REGEN;

    public static Command fromString(String commandName) {
        return Command.valueOf(commandName.toUpperCase());
    }
}
