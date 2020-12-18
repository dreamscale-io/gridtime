package com.dreamscale.gridtime.core.machine.capabilities.cmd;


import java.time.LocalDateTime;

public interface SystemCmd {

    void runCalendarUntil(LocalDateTime runUntilDate);

    void abortCalendarProgram();
}
