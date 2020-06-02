package com.dreamscale.gridtime.core.capability.terminal;

import com.dreamscale.gridtime.api.terminal.Command;

import java.util.List;


public interface TerminalController {


    Command getTerminalCommand();

    List<TerminalRoute> getLocalTerminalRoutes();
}
