package com.dreamscale.gridtime.api.terminal;

public enum CommandGroup {
    INVITE, PROJECT;

    public static CommandGroup fromString(String helpGroup) {
        return CommandGroup.valueOf(helpGroup.toUpperCase());
    }
}
